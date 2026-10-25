package com.kltn.school_hrm.module.payroll.service.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.module.payroll.entity.Payroll;
import com.kltn.school_hrm.module.payroll.entity.PayrollItem;
import com.kltn.school_hrm.module.payroll.entity.PayrollPeriod;
import com.kltn.school_hrm.module.payroll.repository.PayrollItemRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollPeriodRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollRepository;
import com.kltn.school_hrm.module.payroll.service.PayrollService;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayrollPeriodRepository periodRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public PayrollResponse createPayroll(PayrollCreateRequest request) {
        PayrollPeriod period = periodRepository.findById(request.getPeriodId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy kỳ lương: " + request.getPeriodId()));
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên: " + request.getEmployeeId()));

        if (payrollRepository.existsByPeriodIdAndEmployeeId(request.getPeriodId(), request.getEmployeeId())) {
            throw new BusinessException("Bảng lương đã tồn tại cho nhân viên này trong kỳ "
                    + period.getMonth() + "/" + period.getYear());
        }

        Payroll payroll = Payroll.builder()
                .period(period)
                .employee(employee)
                .snapshotEmployeeName(employee.getFullName())
                .snapshotStandardDays(period.getStandardWorkDays())
                .paidWorkDays(request.getPaidWorkDays())
                .totalTeachingHours(request.getTotalTeachingHours())
                .grossSalary(request.getGrossSalary())
                .totalDeductions(request.getTotalDeductions())
                .netSalary(request.getNetSalary())
                .note(request.getNote())
                .status(request.getStatus() != null ? request.getStatus() : PayrollStatus.DRAFT)
                .build();
        payroll = payrollRepository.save(payroll);

        payroll.setItems(saveItems(payroll, request));
        return toResponse(payroll);
    }

    @Override
    public PayrollResponse updatePayroll(Long id, PayrollCreateRequest request) {
        Payroll payroll = getOrThrow(id);
        payroll.guardEditable();

        payroll.setPaidWorkDays(request.getPaidWorkDays());
        payroll.setTotalTeachingHours(request.getTotalTeachingHours());
        payroll.setGrossSalary(request.getGrossSalary());
        payroll.setTotalDeductions(request.getTotalDeductions());
        payroll.setNetSalary(request.getNetSalary());
        payroll.setNote(request.getNote());
        if (request.getStatus() != null) payroll.setStatus(request.getStatus());

        if (request.getItems() != null) {
            payrollItemRepository.deleteByPayrollId(id);
            payroll.setItems(saveItems(payroll, request));
        }

        return toResponse(payrollRepository.save(payroll));
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByPeriodId(Long periodId) {
        return payrollRepository.findByPeriodId(periodId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByMonthAndYear(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PayrollResponse updatePayrollStatus(Long id, PayrollStatus status) {
        Payroll payroll = getOrThrow(id);
        switch (status) {
            case APPROVED -> payroll.approve();
            case PAID -> payroll.markPaid();
            default -> payroll.setStatus(status);
        }
        return toResponse(payrollRepository.save(payroll));
    }

    @Override
    public void deletePayroll(Long id) {
        Payroll payroll = getOrThrow(id);
        payroll.guardEditable();
        payrollRepository.delete(payroll);
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private Payroll getOrThrow(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bảng lương: " + id));
    }

    private List<PayrollItem> saveItems(Payroll payroll, PayrollCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) return new ArrayList<>();
        List<PayrollItem> items = request.getItems().stream()
                .map(i -> PayrollItem.builder()
                        .payroll(payroll)
                        .type(i.getType())
                        .description(i.getDescription())
                        .amount(i.getAmount())
                        .build())
                .collect(Collectors.toList());
        return payrollItemRepository.saveAll(items);
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
                .totalTeachingHours(p.getTotalTeachingHours())
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

