package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.dto.request.LeaveDecisionRequest;
import com.kltn.school_hrm.dto.response.LeaveResponse;
import com.kltn.school_hrm.entity.attendance.LeaveApproval;
import com.kltn.school_hrm.entity.attendance.LeaveBalance;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.LeaveBalanceRepository;
import com.kltn.school_hrm.repository.LeaveRequestRepository;
import com.kltn.school_hrm.repository.UserRepository;
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
    private final UserRepository userRepository;
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // Kiểm tra có đang working không
        if (employee.getStatus() != EmployeeStatus.WORKING) {
            throw new RuntimeException("Nhân viên không đang làm việc");
        }

        // Kiểm tra ngày nghỉ có hợp lệ không
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        // Kiểm tra có đơn nghỉ trùng lặp không
        if (leaveRequestRepository.existsOverlap(
                request.getEmployeeId(),
                BLOCKING_STATUSES,
                request.getStartDate(),
                request.getEndDate())) {
            throw new RuntimeException("Bạn đã có đơn nghỉ trùng lặp");
        }

        // Tính số ngày nghỉ
        int leaveDays = leaveDayCalculator.calculate(request.getStartDate(), request.getEndDate());

        if (leaveDays <= 0) {
            throw new BusinessException("Cần phải nghỉ ít nhất 1 ngày làm việc");
        }

        // Chặn không cho nghỉ qua năm
        if (request.getStartDate().getYear() != request.getEndDate().getYear()) {
            throw new BusinessException("Đơn nghỉ không thể kéo dài qua năm");
        }

        // Kiểm tra số ngày nghỉ và giữ chỗ quỹ phép
        leaveBalanceService.reserve(employee, request.getStartDate().getYear(), leaveDays);

        // Tạo đơn nghỉ
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(leaveDays)
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        // Tạo danh sách phê duyệt tùy theo số ngày nghỉ
        if (leaveDays < 2) {
            // quản lý trực tiếp phê duyệt
            LeaveApproval manager = LeaveApproval.builder()
                    .leaveRequest(leaveRequest)
                    .approver(employee.getDepartment().getManager())
                    .approvalLevel(1)
                    .status(ApprovalStatus.PENDING)
                    .approvedAt(null)
                    .comment(null)
                    .build();
            leaveRequest.setApprovals(List.of(manager));
        } else {
            // quản lý trực tiếp phê duyệt
            LeaveApproval manager = LeaveApproval.builder()
                    .leaveRequest(leaveRequest)
                    .approver(employee.getDepartment().getManager())
                    .approvalLevel(1)
                    .status(ApprovalStatus.PENDING)
                    .approvedAt(null)
                    .comment(null)
                    .build();

            // hiệu trưởng phê duyệt
            LeaveApproval principal = LeaveApproval.builder()
                    .leaveRequest(leaveRequest)
                    .approver(employee.getDepartment().getParentDepartment().getManager())
                    .approvalLevel(2)
                    .status(ApprovalStatus.PENDING)
                    .approvedAt(null)
                    .comment(null)
                    .build();
            leaveRequest.setApprovals(List.of(manager, principal));
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveResponse updateLeaveRequest(Long id, LeaveCreateRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leaveRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Can only update pending leave requests");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());

        if (request.getSubstituteTeacherId() != null) {
            Employee substitute = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new RuntimeException("Substitute teacher not found"));
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
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ"));

        if (!leaveRequest.getStatus().equals(RequestStatus.PENDING)) {
            throw new RuntimeException("Đơn nghỉ đã được xử lý");
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ"));

        leaveApprovalService.rejectCurrentStep(leaveRequest, request.getApproverId(), request.getComment());

        leaveRequest.setStatus(RequestStatus.REJECTED);

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public void deleteLeaveRequest(Long id) {
        if (!leaveRequestRepository.existsById(id)) {
            throw new RuntimeException("Leave request not found");
        }
        leaveRequestRepository.deleteById(id);
    }

    private LeaveResponse mapToResponse(LeaveRequest leaveRequest) {
        return LeaveResponse.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .substituteTeacherId(
                        leaveRequest.getSubstituteTeacher() != null ? leaveRequest.getSubstituteTeacher().getId()
                                : null)
                .approverId(leaveRequest.getApprovals().stream()
                        .map(LeaveApproval::getApprover)
                        .map(Employee::getId)
                        .collect(Collectors.toList()))
                .status(leaveRequest.getStatus())
                .build();
    }
}
