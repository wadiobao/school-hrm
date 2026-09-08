package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.dto.request.LeaveDecisionRequest;
import com.kltn.school_hrm.dto.response.LeaveResponse;
import com.kltn.school_hrm.entity.attendance.LeaveApproval;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.LeaveRequestRepository;
import com.kltn.school_hrm.service.LeaveApprovalService;
import com.kltn.school_hrm.service.LeaveBalanceService;
import com.kltn.school_hrm.service.LeaveService;
import com.kltn.school_hrm.utils.LeaveDayCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveDayCalculator leaveDayCalculator;
    private final LeaveApprovalService leaveApprovalService;

    // Các trạng thái không cho phép tạo đơn nghỉ trùng lặp
    private static final List<RequestStatus> BLOCKING_STATUSES = List.of(
            RequestStatus.PENDING,
            RequestStatus.APPROVED);

    @Override
    @Transactional
    public LeaveResponse createLeaveRequest(LeaveCreateRequest request) {
        // Kiểm tra employee có tồn tại không
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        // Kiểm tra có đang working không
        if (employee.getStatus() != EmployeeStatus.WORKING) {
            throw new BusinessException("Nhân viên không đang làm việc");
        }

        // Kiểm tra ngày nghỉ có hợp lệ không
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        // Chặn không cho nghỉ qua năm
        if (request.getStartDate().getYear() != request.getEndDate().getYear()) {
            throw new BusinessException("Đơn nghỉ không thể kéo dài qua năm");
        }

        // Kiểm tra có đơn nghỉ trùng lặp không
        if (leaveRequestRepository.existsOverlap(
                request.getEmployeeId(),
                BLOCKING_STATUSES,
                request.getStartDate(),
                request.getEndDate())) {
            throw new BusinessException("Bạn đã có đơn nghỉ trùng lặp");
        }

        // Tính số ngày nghỉ
        BigDecimal leaveDays = leaveDayCalculator.calculate(request.getStartDate(), request.getEndDate());

        if (leaveDays == null || leaveDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cần phải nghỉ ít nhất 1 ngày làm việc");
        }

        // Kiểm tra số ngày nghỉ và giữ chỗ quỹ phép
        leaveBalanceService.reserve(employee, request.getStartDate().getYear(), leaveDays);

        Employee substituteTeacher = null;
        if (request.getSubstituteTeacherId() != null) {
            substituteTeacher = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy giáo viên dạy thay"));
        }

        // Tạo đơn nghỉ
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(leaveDays)
                .reason(request.getReason())
                .substituteTeacher(substituteTeacher)
                .status(RequestStatus.PENDING)
                .build();

        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // Ủy thác cho LeaveApprovalService tạo quy trình duyệt
        leaveApprovalService.createApprovalSteps(leaveRequest);

        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveResponse updateLeaveRequest(Long id, LeaveCreateRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn nghỉ"));

        if (leaveRequest.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Chỉ có thể cập nhật đơn nghỉ đang ở trạng thái chờ duyệt");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        if (request.getStartDate().getYear() != request.getEndDate().getYear()) {
            throw new BusinessException("Đơn nghỉ không thể kéo dài qua năm");
        }

        // Kiểm tra overlap ngoại trừ request hiện tại
        if (leaveRequestRepository.existsOverlapExcludingId(
                request.getEmployeeId(),
                id,
                BLOCKING_STATUSES,
                request.getStartDate(),
                request.getEndDate())) {
            throw new BusinessException("Bạn đã có đơn nghỉ trùng lặp trong khoảng thời gian này");
        }

        // Tính lại số ngày nghỉ mới
        BigDecimal newLeaveDays = leaveDayCalculator.calculate(request.getStartDate(), request.getEndDate());
        if (newLeaveDays == null || newLeaveDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cần phải nghỉ ít nhất 1 ngày làm việc");
        }

        // Điều chỉnh lại quỹ phép: hoàn trả pendingDays cũ và reserve pendingDays mới
        leaveBalanceService.release(leaveRequest.getEmployee(), leaveRequest.getStartDate().getYear(), leaveRequest.getTotalDays());
        leaveBalanceService.reserve(employee, request.getStartDate().getYear(), newLeaveDays);

        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setTotalDays(newLeaveDays);
        leaveRequest.setReason(request.getReason());

        if (request.getSubstituteTeacherId() != null) {
            Employee substitute = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy giáo viên dạy thay"));
            leaveRequest.setSubstituteTeacher(substitute);
        } else {
            leaveRequest.setSubstituteTeacher(null);
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    public LeaveResponse getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn nghỉ"));
        return mapToResponse(leaveRequest);
    }

    @Override
    public List<LeaveResponse> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getLeaveRequestsByEmployeeId(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveResponse approveLeaveRequest(Long id, LeaveDecisionRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn nghỉ"));

        if (!RequestStatus.PENDING.equals(leaveRequest.getStatus())) {
            throw new BusinessException("Đơn nghỉ đã được xử lý hoặc không ở trạng thái chờ duyệt");
        }

        leaveApprovalService.approveCurrentStep(leaveRequest, request.getApproverId(), request.getComment());

        if (leaveApprovalService.isFullyApproved(leaveRequest)) {
            leaveRequest.setStatus(RequestStatus.APPROVED);

            leaveBalanceService.consume(
                    leaveRequest.getEmployee(),
                    leaveRequest.getStartDate().getYear(),
                    leaveRequest.getTotalDays());
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeaveRequest(Long id, LeaveDecisionRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn nghỉ"));

        // Guard: Chỉ cho phép từ chối đơn đang ở trạng thái PENDING
        if (!RequestStatus.PENDING.equals(leaveRequest.getStatus())) {
            throw new BusinessException("Đơn nghỉ đã được xử lý hoặc không ở trạng thái chờ duyệt");
        }

        leaveApprovalService.rejectCurrentStep(leaveRequest, request.getApproverId(), request.getComment());

        leaveRequest.setStatus(RequestStatus.REJECTED);
        leaveBalanceService.release(
                leaveRequest.getEmployee(),
                leaveRequest.getStartDate().getYear(),
                leaveRequest.getTotalDays());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    private LeaveResponse mapToResponse(LeaveRequest leaveRequest) {
        List<Long> approverIds = Collections.emptyList();
        if (leaveRequest.getApprovals() != null) {
            approverIds = leaveRequest.getApprovals().stream()
                    .map(LeaveApproval::getApprover)
                    .map(Employee::getId)
                    .collect(Collectors.toList());
        }

        return LeaveResponse.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .totalDays(leaveRequest.getTotalDays())
                .reason(leaveRequest.getReason())
                .substituteTeacherId(
                        leaveRequest.getSubstituteTeacher() != null ? leaveRequest.getSubstituteTeacher().getId()
                                : null)
                .approverId(approverIds)
                .status(leaveRequest.getStatus())
                .build();
    }
}


