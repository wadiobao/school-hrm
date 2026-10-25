package com.kltn.school_hrm.module.payroll.service.implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.attendance.repository.AttendanceRepository;
import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.ContractRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.leave.entity.LeaveRequest;
import com.kltn.school_hrm.module.leave.repository.LeaveRequestRepository;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.module.payroll.entity.Payroll;
import com.kltn.school_hrm.module.payroll.entity.PayrollItem;
import com.kltn.school_hrm.module.payroll.entity.PayrollPeriod;
import com.kltn.school_hrm.module.payroll.repository.PayrollItemRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollPeriodRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollRepository;
import com.kltn.school_hrm.module.payroll.service.PayrollCalculationService;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.LeaveType;
import com.kltn.school_hrm.shared.enums.Enums.PayrollItemType;
import com.kltn.school_hrm.shared.enums.Enums.PayrollPeriodStatus;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai tính lương tự động.
 *
 * <p>Luồng tính toán cho mỗi nhân viên:
 * <ol>
 *   <li>Lấy hợp đồng ACTIVE có hiệu lực trong kỳ → gross salary hợp đồng</li>
 *   <li>Đếm ngày công hợp lệ (PRESENT / LATE / EARLY_LEAVE) trong khoảng ngày kỳ</li>
 *   <li>Tính ngày nghỉ không hưởng lương APPROVED → trừ khỏi ngày công</li>
 *   <li>Tính lương theo công = contractSalary / standardDays × paidDays</li>
 *   <li>Cộng phụ cấp hợp đồng (housing, flight, relocation)</li>
 *   <li>Tính bảo hiểm 10.5% và thuế TNCN đơn giản hoá (8%)</li>
 *   <li>Net = Gross - khấu trừ</li>
 *   <li>Lưu Payroll + PayrollItems</li>
 * </ol>
 * </p>
 *
 * <p><b>Lưu ý:</b> Tỷ lệ thuế/bảo hiểm được đọc từ constant.
 * TODO: chuyển sang PolicyService khi có requirement cụ thể.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    // Tỷ lệ bảo hiểm người lao động đóng (BHXH 8% + BHYT 1.5% + BHTN 1%)
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.105");
    // Thuế TNCN đơn giản hoá — bậc 1 (dưới 5tr): 5%, bậc 2 (5-10tr): 10%
    // Ở đây dùng flat 8% cho demo; thay bằng bảng luỹ tiến khi cần.
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    // Thu nhập cá nhân miễn giảm trừ gia cảnh (11tr/tháng)
    private static final BigDecimal PERSONAL_DEDUCTION = new BigDecimal("11000000");
    // Phiên bản chính sách tính lương đang áp dụng
    private static final String POLICY_VERSION = "v1";

    private final PayrollPeriodRepository periodRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public List<PayrollResponse> calculateForPeriod(Long periodId) {
        PayrollPeriod period = getPeriodOrThrow(periodId);

        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.WORKING
                        || e.getStatus() == EmployeeStatus.PROBATION)
                .collect(Collectors.toList());

        List<PayrollResponse> results = new ArrayList<>();
        for (Employee employee : employees) {
            try {
                results.add(doCalculate(period, employee));
            } catch (BusinessException ex) {
                // Bỏ qua nhân viên không đủ điều kiện (ví dụ: không có hợp đồng)
                log.warn("Skip employee {} ({}): {}", employee.getId(), employee.getFullName(), ex.getMessage());
            }
        }
        return results;
    }

    @Override
    public PayrollResponse calculateForEmployee(Long periodId, Long employeeId) {
        PayrollPeriod period = getPeriodOrThrow(periodId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên: " + employeeId));
        return doCalculate(period, employee);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private PayrollPeriod getPeriodOrThrow(Long periodId) {
        PayrollPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy kỳ lương: " + periodId));
        if (period.getStatus() == PayrollPeriodStatus.LOCKED) {
            throw new BusinessException("Kỳ lương đã chốt, không thể tính lại");
        }
        return period;
    }

    /** Tính lương cho một nhân viên trong kỳ và lưu vào DB (upsert: xóa cũ nếu DRAFT/CONFIRMED) */
    private PayrollResponse doCalculate(PayrollPeriod period, Employee employee) {
        LocalDate start = period.getStartDate();
        LocalDate end = period.getEndDate();
        int standardDays = period.getStandardWorkDays();

        // 0. Upsert: xóa bản ghi cũ nếu tồn tại (trừ PAID)
        payrollRepository.findByPeriodIdAndEmployeeId(period.getId(), employee.getId()).ifPresent(existing -> {
            if (existing.getStatus() == PayrollStatus.PAID) {
                throw new BusinessException("Bảng lương của " + employee.getFullName() + " đã chi trả, không thể tính lại");
            }
            payrollItemRepository.deleteByPayrollId(existing.getId());
            payrollRepository.delete(existing);
            payrollRepository.flush(); // đảm bảo xóa trước khi insert mới
        });

        // 1. Hợp đồng active trong kỳ
        Contract contract = contractRepository
                .findActiveContractForPeriod(employee.getId(), start, end)
                .orElseThrow(() -> new BusinessException(
                        "Nhân viên " + employee.getFullName() + " không có hợp đồng hiệu lực trong kỳ"));


        BigDecimal contractSalary = contract.getGrossSalary() != null
                ? contract.getGrossSalary()
                : BigDecimal.ZERO;

        // 2. Ngày công hợp lệ từ bảng attendance
        long presentDays = attendanceRepository.countByEmployeeAndDateRangeAndStatuses(
                employee.getId(), start, end,
                List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE, AttendanceStatus.EARLY_LEAVE));

        // 3. Ngày nghỉ không lương đã duyệt
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findApprovedByEmployeeAndDateRange(employee.getId(), start, end);

        BigDecimal unpaidLeaveDays = approvedLeaves.stream()
                .filter(lr -> lr.getLeaveType() == LeaveType.UNPAID)
                .map(LeaveRequest::getTotalDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // paidWorkDays = ngày attendance hợp lệ - ngày nghỉ không lương
        BigDecimal paidWorkDays = BigDecimal.valueOf(presentDays)
                .subtract(unpaidLeaveDays)
                .max(BigDecimal.ZERO);

        // 4. Lương theo công
        BigDecimal baseSalary = contractSalary
                .divide(BigDecimal.valueOf(standardDays), 4, RoundingMode.HALF_UP)
                .multiply(paidWorkDays)
                .setScale(0, RoundingMode.HALF_UP);

        // 5. Phụ cấp từ hợp đồng
        BigDecimal housingAllowance = orZero(contract.getHousingAllowance());
        BigDecimal flightAllowance = orZero(contract.getFlightAllowance());
        BigDecimal relocationAllowance = orZero(contract.getRelocationAllowance());
        BigDecimal totalAllowances = housingAllowance.add(flightAllowance).add(relocationAllowance);

        // 6. Gross = base + allowances
        BigDecimal gross = baseSalary.add(totalAllowances);

        // 7. Bảo hiểm (tính trên lương hợp đồng, không phải gross toàn phần)
        BigDecimal insurance = contractSalary.multiply(INSURANCE_RATE)
                .setScale(0, RoundingMode.HALF_UP);

        // 8. Thuế TNCN (gross - personal deduction - insurance)
        BigDecimal taxableIncome = gross.subtract(PERSONAL_DEDUCTION).subtract(insurance);
        BigDecimal tax = taxableIncome.compareTo(BigDecimal.ZERO) > 0
                ? taxableIncome.multiply(TAX_RATE).setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDeductions = insurance.add(tax);
        BigDecimal netSalary = gross.subtract(totalDeductions).max(BigDecimal.ZERO);

        // 9. Lưu Payroll (bao gồm snapshot đầu vào để audit)
        Payroll payroll = Payroll.builder()
                .period(period)
                .employee(employee)
                .snapshotEmployeeName(employee.getFullName())
                .snapshotContractSalary(contractSalary)
                .snapshotStandardDays(standardDays)
                .snapshotUnpaidLeaveDays(unpaidLeaveDays)
                .snapshotTaxableIncome(taxableIncome.max(BigDecimal.ZERO))
                .policyVersion(POLICY_VERSION)
                .paidWorkDays(paidWorkDays)
                .grossSalary(gross)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .status(PayrollStatus.DRAFT)
                .build();
        payroll = payrollRepository.save(payroll);

        // 10. Lưu PayrollItems
        List<PayrollItem> items = new ArrayList<>();
        items.add(item(payroll, PayrollItemType.BASE_SALARY,
                "Lương theo công (" + paidWorkDays.toPlainString() + "/" + standardDays + " ngày)", baseSalary));
        if (housingAllowance.signum() > 0)
            items.add(item(payroll, PayrollItemType.ALLOWANCE, "Phụ cấp nhà ở", housingAllowance));
        if (flightAllowance.signum() > 0)
            items.add(item(payroll, PayrollItemType.ALLOWANCE, "Phụ cấp đi lại", flightAllowance));
        if (relocationAllowance.signum() > 0)
            items.add(item(payroll, PayrollItemType.ALLOWANCE, "Phụ cấp di chuyển", relocationAllowance));
        items.add(item(payroll, PayrollItemType.INSURANCE,
                "Bảo hiểm NLĐ (10.5% lương HĐ)", insurance));
        if (tax.signum() > 0)
            items.add(item(payroll, PayrollItemType.TAX, "Thuế TNCN (8% thu nhập chịu thuế)", tax));

        payrollItemRepository.saveAll(items);
        payroll.setItems(items);

        return toResponse(payroll);
    }

    private PayrollItem item(Payroll payroll, PayrollItemType type, String desc, BigDecimal amount) {
        return PayrollItem.builder()
                .payroll(payroll)
                .type(type)
                .description(desc)
                .amount(amount)
                .build();
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private PayrollResponse toResponse(Payroll p) {
        List<PayrollResponse.PayrollItemResponse> itemResponses = p.getItems() == null
                ? List.of()
                : p.getItems().stream()
                        .map(i -> PayrollResponse.PayrollItemResponse.builder()
                                .id(i.getId())
                                .type(i.getType())
                                .description(i.getDescription())
                                .amount(i.getAmount())
                                .build())
                        .collect(Collectors.toList());

        return PayrollResponse.builder()
                .id(p.getId())
                .periodId(p.getPeriod().getId())
                .month(p.getPeriod().getMonth())
                .year(p.getPeriod().getYear())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getSnapshotEmployeeName())
                .snapshotContractSalary(p.getSnapshotContractSalary())
                .snapshotStandardDays(p.getSnapshotStandardDays())
                .paidWorkDays(p.getPaidWorkDays())
                .snapshotUnpaidLeaveDays(p.getSnapshotUnpaidLeaveDays())
                .snapshotTaxableIncome(p.getSnapshotTaxableIncome())
                .policyVersion(p.getPolicyVersion())
                .grossSalary(p.getGrossSalary())
                .totalDeductions(p.getTotalDeductions())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .note(p.getNote())
                .calculatedAt(p.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}

