package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.dto.request.LeaveDecisionRequest;
import com.kltn.school_hrm.dto.response.ApprovalActionResponse;
import com.kltn.school_hrm.dto.response.ApprovalResult;
import com.kltn.school_hrm.dto.response.ApprovalStepResponse;
import com.kltn.school_hrm.dto.response.LeaveResponse;
import com.kltn.school_hrm.entity.approval.ApprovalAction;
import com.kltn.school_hrm.entity.approval.ApprovalRequest;
import com.kltn.school_hrm.entity.approval.ApprovalRequestStep;
import com.kltn.school_hrm.entity.attendance.LeaveApproval;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.ApprovalActionRepository;
import com.kltn.school_hrm.repository.ApprovalRequestRepository;
import com.kltn.school_hrm.repository.ApprovalRequestStepRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.LeaveRequestRepository;
import com.kltn.school_hrm.service.LeaveApprovalService;
import com.kltn.school_hrm.service.LeaveBalanceService;
import com.kltn.school_hrm.service.LeaveRequestService;
import com.kltn.school_hrm.service.approval.ApprovalEngineService;
import com.kltn.school_hrm.utils.LeaveDayCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveDayCalculator leaveDayCalculator;
    private final LeaveApprovalService leaveApprovalService;
    private final ApprovalEngineService approvalEngineService;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRequestStepRepository approvalRequestStepRepository;
    private final ApprovalActionRepository approvalActionRepository;

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

        // Khởi tạo quy trình phê duyệt tổng quát (Approval Engine)
        java.util.Map<String, Object> context = java.util.Map.of("totalDays", leaveDays);
        approvalEngineService.initiateRequest(
                "LEAVE_REQUEST",
                leaveRequest.getId(),
                "LR-" + leaveRequest.getId(),
                employee,
                context);

        // Giữ lại tạo dữ liệu tương thích cũ (nếu cần)
        try {
            leaveApprovalService.createApprovalSteps(leaveRequest);
        } catch (Exception e) {
            // Log nhưng không làm gián đoạn flow mới
        }

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

        // 1. Thực hiện phê duyệt qua Generic Approval Engine
        ApprovalResult result = approvalEngineService.approve(
                "LEAVE_REQUEST",
                id,
                request.getApproverId(),
                request.getComment());

        // 2. Đồng bộ tương thích hệ thống cũ (nếu có)
        try {
            leaveApprovalService.approveCurrentStep(leaveRequest, request.getApproverId(), request.getComment());
        } catch (Exception e) {
            // Không làm gián đoạn luồng mới
        }

        // 3. Xử lý nghiệp vụ khi đơn nghỉ được phê duyệt hoàn toàn
        if (result.isFullyApproved()) {
            leaveRequest.setStatus(RequestStatus.APPROVED);

            leaveBalanceService.consume(
                    leaveRequest.getEmployee(),
                    leaveRequest.getStartDate().getYear(),
                    leaveRequest.getTotalDays(),
                    leaveRequest);
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

        // 1. Thực hiện từ chối qua Generic Approval Engine
        approvalEngineService.reject(
                "LEAVE_REQUEST",
                id,
                request.getApproverId(),
                request.getComment());

        // 2. Đồng bộ tương thích hệ thống cũ (nếu có)
        try {
            leaveApprovalService.rejectCurrentStep(leaveRequest, request.getApproverId(), request.getComment());
        } catch (Exception e) {
            // Không làm gián đoạn luồng mới
        }

        // 3. Xử lý nghiệp vụ hoàn trả ngày phép
        leaveRequest.setStatus(RequestStatus.REJECTED);
        leaveBalanceService.release(
                leaveRequest.getEmployee(),
                leaveRequest.getStartDate().getYear(),
                leaveRequest.getTotalDays());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }


    @Override
    public List<LeaveResponse> getOverdueLeaveRequests() {
        LocalDateTime now = LocalDateTime.now();

        return leaveRequestRepository.findByStatus(RequestStatus.PENDING).stream()
                .filter(req -> isOverdue(req, now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<LeaveResponse> processOverdueLeaveRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByStatus(RequestStatus.PENDING);
        List<LeaveRequest> overdueRequests = new ArrayList<>();

        for (LeaveRequest req : pendingRequests) {
            if (isOverdue(req, now)) {
                req.setStatus(RequestStatus.OVERDUE);

                // 1. Hủy trên Generic Approval Engine
                try {
                    approvalEngineService.cancel(
                            "LEAVE_REQUEST",
                            req.getId(),
                            null,
                            "Tự động hủy do quá hạn thời gian chờ phê duyệt");
                } catch (Exception e) {
                    // Log nếu đã được xử lý hoặc chưa khởi tạo approval request
                }

                // 2. Hủy các bước phê duyệt legacy (nếu có)
                if (req.getApprovals() != null) {
                    for (LeaveApproval approval : req.getApprovals()) {
                        if (approval.getStatus() == ApprovalStatus.PENDING) {
                            approval.setStatus(ApprovalStatus.REJECTED);
                            approval.setComment("Tự động từ chối do quá hạn phê duyệt");
                            approval.setApprovedAt(now);
                        }
                    }
                }

                // 3. Giải phóng quỹ phép pending
                leaveBalanceService.release(
                        req.getEmployee(),
                        req.getStartDate().getYear(),
                        req.getTotalDays());

                overdueRequests.add(leaveRequestRepository.save(req));
            }
        }

        return overdueRequests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean isOverdue(LeaveRequest req, LocalDateTime now) {
        if (req.getTotalDays() == null || req.getTotalDays().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Ngưỡng quá hạn: số ngày nghỉ / 2 (tính theo giờ: (totalDays / 2) * 24h)
        double thresholdHours = req.getTotalDays().doubleValue() / 2.0 * 24.0;

        LocalDateTime referenceTime = req.getCreatedAt() != null
                ? req.getCreatedAt()
                : (req.getStartDate() != null ? req.getStartDate().atStartOfDay() : now);

        long waitingHours = java.time.Duration.between(referenceTime, now).toHours();
        return waitingHours > thresholdHours;
    }

    private LeaveResponse mapToResponse(LeaveRequest leaveRequest) {
        List<Long> approverIds = new ArrayList<>();
        List<ApprovalStepResponse> approvalStepResponses = new ArrayList<>();
        List<ApprovalActionResponse> approvalActionResponses = new ArrayList<>();

        // 1. Lấy thông tin steps và actions từ Generic Approval Framework
        approvalRequestRepository.findByBusinessTypeAndBusinessId("LEAVE_REQUEST", leaveRequest.getId())
                .ifPresent(approvalReq -> {
                    List<ApprovalRequestStep> steps = approvalRequestStepRepository
                            .findByApprovalRequestIdOrderByLevelOrderAsc(approvalReq.getId());
                    for (ApprovalRequestStep step : steps) {
                        Long approverId = step.getAssignedApprover() != null ? step.getAssignedApprover().getId() : null;
                        String approverName = step.getAssignedApprover() != null ? step.getAssignedApprover().getFullName() : null;
                        if (approverId != null && !approverIds.contains(approverId)) {
                            approverIds.add(approverId);
                        }

                        approvalStepResponses.add(ApprovalStepResponse.builder()
                                .id(step.getId())
                                .levelOrder(step.getLevelOrder())
                                .levelName(step.getLevelName())
                                .approverType(step.getApproverType())
                                .assignedApproverId(approverId)
                                .assignedApproverName(approverName)
                                .status(step.getStatus())
                                .assignedAt(step.getAssignedAt())
                                .completedAt(step.getCompletedAt())
                                .build());
                    }

                    List<ApprovalAction> actions = approvalActionRepository
                            .findByApprovalRequestIdOrderByActionAtAsc(approvalReq.getId());
                    for (ApprovalAction action : actions) {
                        Long actorId = action.getActor() != null ? action.getActor().getId() : null;
                        String actorName = action.getActor() != null ? action.getActor().getFullName() : "Hệ thống";

                        approvalActionResponses.add(ApprovalActionResponse.builder()
                                .id(action.getId())
                                .actorId(actorId)
                                .actorName(actorName)
                                .action(action.getAction())
                                .comment(action.getComment())
                                .actionAt(action.getActionAt())
                                .build());
                    }
                });

        // 2. Fallback sang legacy approvers nếu approverIds rỗng
        if (approverIds.isEmpty() && leaveRequest.getApprovals() != null) {
            List<Long> currentApproverIds = new ArrayList<>();
            currentApproverIds = leaveRequest.getApprovals().stream()
                    .map(LeaveApproval::getApprover)
                    .map(Employee::getId)
                    .collect(Collectors.toList());

            approverIds.addAll(currentApproverIds);
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
                .approvalSteps(approvalStepResponses)
                .approvalActions(approvalActionResponses)
                .build();
    }
}


