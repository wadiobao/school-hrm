package com.kltn.school_hrm.module.payroll.service.implement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollPaymentRequest;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollTransitionRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollHistoryResponse;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollPaymentResponse;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.module.payroll.entity.Payroll;
import com.kltn.school_hrm.module.payroll.entity.PayrollHistory;
import com.kltn.school_hrm.module.payroll.entity.PayrollItem;
import com.kltn.school_hrm.module.payroll.entity.PayrollPayment;
import com.kltn.school_hrm.module.payroll.entity.PayrollPeriod;
import com.kltn.school_hrm.module.payroll.entity.PayrollSnapshot;
import com.kltn.school_hrm.module.payroll.repository.PayrollHistoryRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollItemRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollPaymentRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollPeriodRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollRepository;
import com.kltn.school_hrm.module.payroll.repository.PayrollSnapshotRepository;
import com.kltn.school_hrm.module.payroll.service.PayrollService;
import com.kltn.school_hrm.module.payroll.statemachine.PayrollStateMachine;
import com.kltn.school_hrm.shared.enums.Enums.PaymentStatus;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai nghiệp vụ quản lý bảng lương và điều phối vòng đời theo State Machine (req 4, 5, 6, 7).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayrollPeriodRepository periodRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollHistoryRepository payrollHistoryRepository;
    private final PayrollPaymentRepository payrollPaymentRepository;
    private final PayrollSnapshotRepository payrollSnapshotRepository;
    private final PayrollStateMachine payrollStateMachine;

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

        // Ghi nhận lịch sử tạo ban đầu
        payrollHistoryRepository.save(PayrollHistory.of(
                payroll,
                null,
                payroll.getStatus(),
                null,
                "SYSTEM",
                "Tạo bảng lương mới"
        ));

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
        if (request.getStatus() != null) {
            payroll.setStatus(request.getStatus());
        }

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
        return transitionStatus(
                id,
                PayrollTransitionRequest.builder().targetStatus(status).reason("Cập nhật trạng thái trực tiếp").build(),
                null,
                "SYSTEM"
        );
    }

    @Override
    public PayrollResponse transitionStatus(Long id, PayrollTransitionRequest request, Long currentUserId, String currentUserName) {
        // 1. Pessimistic Locking ngăn hai người cùng chuyển trạng thái hoặc ghi đè đồng thời (req 4)
        Payroll payroll = payrollRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bảng lương: " + id));

        PayrollStatus current = payroll.getStatus();
        PayrollStatus target = request.getTargetStatus();

        if (current == target) {
            return toResponse(payroll);
        }

        // 2. State Machine: kiểm tra tính hợp lệ của luồng chuyển đổi (req 2 & 4)
        payrollStateMachine.validateTransition(current, target);

        // 3. Kiểm tra các quy tắc nghiệp vụ quan trọng (req 6 & 7)
        validateBusinessRules(payroll, current, target, request.getReason(), currentUserId, currentUserName);

        // 4. Cập nhật trạng thái
        payroll.changeStatus(target);
        Payroll updatedPayroll = payrollRepository.save(payroll);

        // 5. Lưu vết kiểm toán (Audit Trail)
        payrollHistoryRepository.save(PayrollHistory.of(
                updatedPayroll,
                current,
                target,
                currentUserId,
                currentUserName,
                request.getReason()
        ));

        log.info("Payroll #{} transitioned from {} to {} by user {} (reason: {})",
                id, current, target, currentUserName, request.getReason());

        return toResponse(updatedPayroll);
    }

    @Override
    public PayrollPaymentResponse recordPayment(Long id, PayrollPaymentRequest request, Long currentUserId, String currentUserName) {
        // Pessimistic lock trên bảng lương để kiểm soát số tiền chi trả nhất quán
        Payroll payroll = payrollRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bảng lương: " + id));

        if (payroll.getStatus() != PayrollStatus.LOCKED && payroll.getStatus() != PayrollStatus.PAID) {
            throw new BusinessException("Bảng lương phải ở trạng thái ĐÃ CHỐT (LOCKED) mới có thể thực hiện thanh toán chi trả");
        }

        PayrollPayment payment = PayrollPayment.builder()
                .payroll(payroll)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .referenceCode(request.getReferenceCode())
                .status(PaymentStatus.COMPLETED)
                .note(request.getNote())
                .paidByUserId(currentUserId)
                .paidByName(currentUserName)
                .build();

        payment = payrollPaymentRepository.save(payment);

        // Kiểm tra tổng số tiền đã thanh toán hoàn tất
        BigDecimal totalPaid = payrollPaymentRepository.sumAmountByPayrollIdAndStatus(payroll.getId(), PaymentStatus.COMPLETED);
        BigDecimal netSalary = payroll.getNetSalary() != null ? payroll.getNetSalary() : BigDecimal.ZERO;

        // Nếu tổng chi trả >= netSalary và bảng lương đang ở LOCKED -> tự động chuyển sang PAID (req 6 & 7)
        if (totalPaid.compareTo(netSalary) >= 0 && payroll.getStatus() == PayrollStatus.LOCKED) {
            payroll.changeStatus(PayrollStatus.PAID);
            payrollRepository.save(payroll);

            payrollHistoryRepository.save(PayrollHistory.of(
                    payroll,
                    PayrollStatus.LOCKED,
                    PayrollStatus.PAID,
                    currentUserId,
                    currentUserName,
                    "Tự động hoàn tất chi trả sang PAID sau khi thanh toán đủ: " + totalPaid + " / " + netSalary
            ));
            log.info("Payroll #{} automatically moved to PAID as total paid {} >= net salary {}", id, totalPaid, netSalary);
        }

        return toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollHistoryResponse> getHistoriesByPayrollId(Long id) {
        // Đảm bảo bảng lương tồn tại
        getOrThrow(id);
        return payrollHistoryRepository.findByPayrollIdOrderByChangedAtDesc(id).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPaymentResponse> getPaymentsByPayrollId(Long id) {
        getOrThrow(id);
        return payrollPaymentRepository.findByPayrollIdOrderByPaymentDateDesc(id).stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePayroll(Long id) {
        Payroll payroll = getOrThrow(id);
        payroll.guardEditable();
        payrollRepository.delete(payroll);
    }

    // ─── Private helpers & Business Validations ─────────────────────────────────

    /**
     * Xác thực các quy tắc nghiệp vụ chặt chẽ trước khi chấp thuận chuyển trạng thái (req 6 & 7).
     */
    private void validateBusinessRules(
            Payroll payroll,
            PayrollStatus current,
            PayrollStatus target,
            String reason,
            Long currentUserId,
            String currentUserName
    ) {
        // Quy tắc khi chuyển sang LOCKED: Phải kiểm tra tổng lương, dữ liệu và đóng băng Snapshot
        if (target == PayrollStatus.LOCKED) {
            if (payroll.getNetSalary() == null || payroll.getGrossSalary() == null) {
                throw new BusinessException("Không thể chốt bảng lương chưa có đầy đủ thông tin lương Gross/Net");
            }

            List<PayrollItem> items = payrollItemRepository.findByPayrollId(payroll.getId());
            if (items.isEmpty()) {
                throw new BusinessException("Không thể chốt bảng lương không có bất kỳ khoản chi tiết lương (PayrollItem) nào");
            }

            // Đóng băng dữ liệu Snapshot bất biến (req 5 & 6)
            freezeSnapshot(payroll, items, currentUserId, currentUserName);
        }

        // Quy tắc khi mở khóa LOCKED -> REVIEW: Bắt buộc có lý do giải trình
        if (current == PayrollStatus.LOCKED && target == PayrollStatus.REVIEW) {
            if (reason == null || reason.trim().isBlank()) {
                throw new BusinessException("Bắt buộc phải cung cấp lý do giải trình khi mở khóa bảng lương đã chốt");
            }
        }

        // Quy tắc khi chuyển sang PAID: Phải đảm bảo Finance đã thanh toán đầy đủ
        if (target == PayrollStatus.PAID) {
            BigDecimal totalPaid = payrollPaymentRepository.sumAmountByPayrollIdAndStatus(payroll.getId(), PaymentStatus.COMPLETED);
            BigDecimal netSalary = payroll.getNetSalary() != null ? payroll.getNetSalary() : BigDecimal.ZERO;
            if (totalPaid.compareTo(netSalary) < 0) {
                throw new BusinessException(String.format(
                        "Chưa thể chuyển sang PAID. Tổng số tiền đã thanh toán (%s) chưa đủ nghĩa vụ chi trả Net (%s). Còn thiếu %s.",
                        totalPaid.toPlainString(),
                        netSalary.toPlainString(),
                        netSalary.subtract(totalPaid).toPlainString()
                ));
            }
        }
    }

    /**
     * Tạo và đóng băng dữ liệu snapshot tại thời điểm LOCKED.
     */
    private void freezeSnapshot(Payroll payroll, List<PayrollItem> items, Long lockedByUserId, String lockedByName) {
        String itemsJson = serializeItemsSummary(items);

        PayrollSnapshot snapshot = payrollSnapshotRepository.findByPayrollId(payroll.getId())
                .orElse(PayrollSnapshot.builder().payroll(payroll).build());

        snapshot.setSnapshotEmployeeName(payroll.getSnapshotEmployeeName());
        snapshot.setSnapshotContractSalary(payroll.getSnapshotContractSalary());
        snapshot.setSnapshotStandardDays(payroll.getSnapshotStandardDays());
        snapshot.setSnapshotPaidWorkDays(payroll.getPaidWorkDays());
        snapshot.setSnapshotTotalTeachingHours(payroll.getTotalTeachingHours());
        snapshot.setSnapshotUnpaidLeaveDays(payroll.getSnapshotUnpaidLeaveDays());
        snapshot.setSnapshotTaxableIncome(payroll.getSnapshotTaxableIncome());
        snapshot.setPolicyVersion(payroll.getPolicyVersion());
        snapshot.setSnapshotGrossSalary(payroll.getGrossSalary());
        snapshot.setSnapshotTotalDeductions(payroll.getTotalDeductions());
        snapshot.setSnapshotNetSalary(payroll.getNetSalary());
        snapshot.setItemsSummaryJson(itemsJson);
        snapshot.setLockedAt(LocalDateTime.now());
        snapshot.setLockedByUserId(lockedByUserId);
        snapshot.setLockedByName(lockedByName);

        payrollSnapshotRepository.save(snapshot);
    }

    private String serializeItemsSummary(List<PayrollItem> items) {
        if (items == null || items.isEmpty()) return "[]";
        return items.stream()
                .map(i -> String.format(
                        "{\"type\":\"%s\",\"description\":\"%s\",\"amount\":%s}",
                        escapeJson(i.getType() != null ? i.getType().name() : ""),
                        escapeJson(i.getDescription() != null ? i.getDescription() : ""),
                        i.getAmount() != null ? i.getAmount().toPlainString() : "0"
                ))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

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

        BigDecimal totalPaid = payrollPaymentRepository.sumAmountByPayrollIdAndStatus(p.getId(), PaymentStatus.COMPLETED);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;
        BigDecimal netSalary = p.getNetSalary() != null ? p.getNetSalary() : BigDecimal.ZERO;
        BigDecimal remaining = netSalary.subtract(totalPaid).max(BigDecimal.ZERO);
        boolean isFullyPaid = totalPaid.compareTo(netSalary) >= 0 && netSalary.compareTo(BigDecimal.ZERO) > 0;

        String employeeCode = p.getEmployee() != null ? p.getEmployee().getEmployeeCode() : null;

        return PayrollResponse.builder()
                .id(p.getId())
                .periodId(p.getPeriod().getId())
                .month(p.getPeriod().getMonth())
                .year(p.getPeriod().getYear())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getSnapshotEmployeeName())
                .employeeCode(employeeCode)
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
                .totalPaidAmount(totalPaid)
                .remainingAmount(remaining)
                .isFullyPaid(isFullyPaid)
                .status(p.getStatus())
                .note(p.getNote())
                .calculatedAt(p.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    private PayrollHistoryResponse toHistoryResponse(PayrollHistory h) {
        return PayrollHistoryResponse.builder()
                .id(h.getId())
                .payrollId(h.getPayroll().getId())
                .fromStatus(h.getFromStatus())
                .fromStatusLabel(h.getFromStatus() != null ? h.getFromStatus().getLabel() : "Khởi tạo")
                .toStatus(h.getToStatus())
                .toStatusLabel(h.getToStatus() != null ? h.getToStatus().getLabel() : null)
                .changedByUserId(h.getChangedByUserId())
                .changedByName(h.getChangedByName())
                .changedAt(h.getChangedAt())
                .reason(h.getReason())
                .build();
    }

    private PayrollPaymentResponse toPaymentResponse(PayrollPayment p) {
        return PayrollPaymentResponse.builder()
                .id(p.getId())
                .payrollId(p.getPayroll().getId())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .paymentMethod(p.getPaymentMethod())
                .paymentMethodLabel(p.getPaymentMethod() != null ? p.getPaymentMethod().getLabel() : null)
                .referenceCode(p.getReferenceCode())
                .status(p.getStatus())
                .statusLabel(p.getStatus() != null ? p.getStatus().getLabel() : null)
                .note(p.getNote())
                .paidByUserId(p.getPaidByUserId())
                .paidByName(p.getPaidByName())
                .build();
    }
}
