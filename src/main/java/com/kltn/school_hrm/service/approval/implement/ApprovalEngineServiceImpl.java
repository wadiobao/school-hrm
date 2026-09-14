package com.kltn.school_hrm.service.approval.implement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.response.ApprovalResult;
import com.kltn.school_hrm.entity.approval.ApprovalAction;
import com.kltn.school_hrm.entity.approval.ApprovalLevel;
import com.kltn.school_hrm.entity.approval.ApprovalPolicy;
import com.kltn.school_hrm.entity.approval.ApprovalRequest;
import com.kltn.school_hrm.entity.approval.ApprovalRequestStep;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ApprovalActionType;
import com.kltn.school_hrm.enums.Enums.ApprovalConditionType;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.ApprovalActionRepository;
import com.kltn.school_hrm.repository.ApprovalLevelRepository;
import com.kltn.school_hrm.repository.ApprovalPolicyRepository;
import com.kltn.school_hrm.repository.ApprovalRequestRepository;
import com.kltn.school_hrm.repository.ApprovalRequestStepRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.approval.ApprovalEngineService;
import com.kltn.school_hrm.service.approval.ApproverResolverService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ApprovalEngineServiceImpl implements ApprovalEngineService {

    private final ApprovalPolicyRepository approvalPolicyRepository;
    private final ApprovalLevelRepository approvalLevelRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRequestStepRepository approvalRequestStepRepository;
    private final ApprovalActionRepository approvalActionRepository;
    private final ApproverResolverService approverResolverService;
    private final EmployeeRepository employeeRepository;

    @Override
    public ApprovalRequest initiateRequest(
            String businessType,
            Long businessId,
            String businessRefCode,
            Employee requester,
            Map<String, Object> contextVariables) {

        // 1. Tìm policy active mới nhất cho businessType
        ApprovalPolicy policy = approvalPolicyRepository
                .findFirstByBusinessTypeAndIsActiveTrueOrderByVersionDesc(businessType)
                .orElseThrow(() -> new BusinessException("Không tìm thấy chính sách phê duyệt đang hiệu lực cho: " + businessType));

        List<ApprovalLevel> levels = approvalLevelRepository.findByPolicyIdOrderByLevelOrderAsc(policy.getId());
        if (levels.isEmpty()) {
            throw new BusinessException("Chính sách phê duyệt chưa được cấu hình các cấp duyệt");
        }

        // 2. Tạo ApprovalRequest
        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .businessType(businessType)
                .businessId(businessId)
                .businessRefCode(businessRefCode)
                .policy(policy)
                .policyVersion(policy.getVersion())
                .currentLevel(1)
                .status(ApprovalStatus.PENDING)
                .requester(requester)
                .steps(new ArrayList<>())
                .actions(new ArrayList<>())
                .build();

        approvalRequest = approvalRequestRepository.save(approvalRequest);

        // 3. Đánh giá điều kiện và resolve approver cho từng step
        int activeStepOrder = 1;
        for (ApprovalLevel level : levels) {
            if (!evaluateCondition(level, contextVariables)) {
                log.info("Bỏ qua cấp duyệt '{}' do không thỏa điều kiện áp dụng", level.getLevelName());
                continue;
            }

            Employee assignedApprover = approverResolverService.resolveApprover(level, requester);

            ApprovalRequestStep step = ApprovalRequestStep.builder()
                    .approvalRequest(approvalRequest)
                    .levelOrder(activeStepOrder++)
                    .levelName(level.getLevelName())
                    .approverType(level.getApproverType())
                    .assignedApprover(assignedApprover)
                    .status(ApprovalStatus.PENDING)
                    .conditionSnapshot(formatConditionSnapshot(level))
                    .assignedAt(LocalDateTime.now())
                    .build();

            step = approvalRequestStepRepository.save(step);
            approvalRequest.getSteps().add(step);
        }

        if (approvalRequest.getSteps().isEmpty()) {
            throw new BusinessException("Không có cấp duyệt nào phù hợp với yêu cầu này");
        }

        // 4. Ghi action SUBMIT
        ApprovalAction submitAction = ApprovalAction.builder()
                .approvalRequest(approvalRequest)
                .approvalRequestStep(null)
                .actor(requester)
                .action(ApprovalActionType.SUBMIT)
                .comment("Nộp yêu cầu phê duyệt")
                .actionAt(LocalDateTime.now())
                .build();

        approvalActionRepository.save(submitAction);
        approvalRequest.getActions().add(submitAction);

        return approvalRequest;
    }

    @Override
    public ApprovalResult approve(String businessType, Long businessId, Long approverId, String comment) {
        ApprovalRequest request = getPendingRequest(businessType, businessId);
        ApprovalRequestStep currentStep = getCurrentStep(request);

        // Kiểm tra quyền duyệt
        if (!currentStep.getAssignedApprover().getId().equals(approverId)) {
            throw new BusinessException("Bạn không phải người có thẩm quyền phê duyệt ở cấp hiện tại (" + currentStep.getLevelName() + ")");
        }

        Employee actor = currentStep.getAssignedApprover();
        LocalDateTime now = LocalDateTime.now();

        // Cập nhật currentStep
        currentStep.setStatus(ApprovalStatus.APPROVED);
        currentStep.setCompletedAt(now);
        approvalRequestStepRepository.save(currentStep);

        // Ghi action APPROVE
        ApprovalAction action = ApprovalAction.builder()
                .approvalRequest(request)
                .approvalRequestStep(currentStep)
                .actor(actor)
                .action(ApprovalActionType.APPROVE)
                .comment(comment != null && !comment.isBlank() ? comment : "Đồng ý phê duyệt")
                .actionAt(now)
                .build();
        approvalActionRepository.save(action);

        // Kiểm tra còn step tiếp theo không
        int nextLevel = request.getCurrentLevel() + 1;
        boolean hasNextStep = approvalRequestStepRepository
                .findByApprovalRequestIdAndLevelOrder(request.getId(), nextLevel)
                .isPresent();

        boolean fullyApproved = false;
        if (hasNextStep) {
            request.setCurrentLevel(nextLevel);
        } else {
            request.setStatus(ApprovalStatus.APPROVED);
            fullyApproved = true;
        }
        approvalRequestRepository.save(request);

        return ApprovalResult.builder()
                .approvalRequestId(request.getId())
                .businessType(businessType)
                .businessId(businessId)
                .currentLevel(request.getCurrentLevel())
                .status(request.getStatus())
                .fullyApproved(fullyApproved)
                .rejected(false)
                .currentApproverId(actor.getId())
                .currentApproverName(actor.getFullName())
                .comment(action.getComment())
                .actionAt(now)
                .build();
    }

    @Override
    public ApprovalResult reject(String businessType, Long businessId, Long approverId, String comment) {
        ApprovalRequest request = getPendingRequest(businessType, businessId);
        ApprovalRequestStep currentStep = getCurrentStep(request);

        // Kiểm tra quyền duyệt
        if (!currentStep.getAssignedApprover().getId().equals(approverId)) {
            throw new BusinessException("Bạn không phải người có thẩm quyền từ chối ở cấp hiện tại (" + currentStep.getLevelName() + ")");
        }

        Employee actor = currentStep.getAssignedApprover();
        LocalDateTime now = LocalDateTime.now();

        // Cập nhật currentStep
        currentStep.setStatus(ApprovalStatus.REJECTED);
        currentStep.setCompletedAt(now);
        approvalRequestStepRepository.save(currentStep);

        // Đánh dấu các step chưa duyệt sau đó thành SKIPPED
        List<ApprovalRequestStep> allSteps = approvalRequestStepRepository.findByApprovalRequestIdOrderByLevelOrderAsc(request.getId());
        for (ApprovalRequestStep step : allSteps) {
            if (step.getLevelOrder() > currentStep.getLevelOrder() && step.getStatus() == ApprovalStatus.PENDING) {
                step.setStatus(ApprovalStatus.SKIPPED);
                step.setCompletedAt(now);
                approvalRequestStepRepository.save(step);
            }
        }

        // Ghi action REJECT
        ApprovalAction action = ApprovalAction.builder()
                .approvalRequest(request)
                .approvalRequestStep(currentStep)
                .actor(actor)
                .action(ApprovalActionType.REJECT)
                .comment(comment != null && !comment.isBlank() ? comment : "Từ chối yêu cầu")
                .actionAt(now)
                .build();
        approvalActionRepository.save(action);

        // Đánh dấu request REJECTED
        request.setStatus(ApprovalStatus.REJECTED);
        approvalRequestRepository.save(request);

        return ApprovalResult.builder()
                .approvalRequestId(request.getId())
                .businessType(businessType)
                .businessId(businessId)
                .currentLevel(request.getCurrentLevel())
                .status(ApprovalStatus.REJECTED)
                .fullyApproved(false)
                .rejected(true)
                .currentApproverId(actor.getId())
                .currentApproverName(actor.getFullName())
                .comment(action.getComment())
                .actionAt(now)
                .build();
    }

    @Override
    public ApprovalResult cancel(String businessType, Long businessId, Long actorId, String comment) {
        ApprovalRequest request = getPendingRequest(businessType, businessId);
        Employee actor = null;
        if (actorId != null) {
            actor = employeeRepository.findById(actorId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy người thực hiện hủy yêu cầu"));
        }

        LocalDateTime now = LocalDateTime.now();

        // Đánh dấu tất cả các step pending thành CANCELLED
        List<ApprovalRequestStep> steps = approvalRequestStepRepository.findByApprovalRequestIdOrderByLevelOrderAsc(request.getId());
        for (ApprovalRequestStep step : steps) {
            if (step.getStatus() == ApprovalStatus.PENDING) {
                step.setStatus(ApprovalStatus.CANCELLED);
                step.setCompletedAt(now);
                approvalRequestStepRepository.save(step);
            }
        }

        // Ghi action CANCEL
        ApprovalAction action = ApprovalAction.builder()
                .approvalRequest(request)
                .approvalRequestStep(null)
                .actor(actor)
                .action(ApprovalActionType.CANCEL)
                .comment(comment != null && !comment.isBlank() ? comment : "Hủy yêu cầu phê duyệt")
                .actionAt(now)
                .build();
        approvalActionRepository.save(action);

        request.setStatus(ApprovalStatus.CANCELLED);
        approvalRequestRepository.save(request);

        return ApprovalResult.builder()
                .approvalRequestId(request.getId())
                .businessType(businessType)
                .businessId(businessId)
                .currentLevel(request.getCurrentLevel())
                .status(ApprovalStatus.CANCELLED)
                .fullyApproved(false)
                .rejected(false)
                .currentApproverId(actor != null ? actor.getId() : null)
                .currentApproverName(actor != null ? actor.getFullName() : "Hệ thống")
                .comment(action.getComment())
                .actionAt(now)
                .build();
    }

    @Override
    public void forwardPendingApprovals(Long oldApproverId, Long newApproverId, Long actorId, String comment) {
        List<ApprovalRequestStep> pendingSteps = approvalRequestStepRepository
                .findByAssignedApproverIdAndStatus(oldApproverId, ApprovalStatus.PENDING);

        if (pendingSteps.isEmpty()) {
            return;
        }

        Employee newApprover = employeeRepository.findById(newApproverId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người tiếp nhận chuyển giao"));

        Employee actor = (actorId != null)
                ? employeeRepository.findById(actorId).orElse(newApprover)
                : newApprover;

        LocalDateTime now = LocalDateTime.now();

        for (ApprovalRequestStep step : pendingSteps) {
            // Ghi nhận action FORWARD trước khi đổi assignedApprover
            ApprovalAction action = ApprovalAction.builder()
                    .approvalRequest(step.getApprovalRequest())
                    .approvalRequestStep(step)
                    .actor(actor)
                    .action(ApprovalActionType.FORWARD)
                    .comment(comment != null && !comment.isBlank() ? comment : "Chuyển giao quyền duyệt sang " + newApprover.getFullName())
                    .actionAt(now)
                    .build();
            approvalActionRepository.save(action);

            step.setAssignedApprover(newApprover);
            step.setAssignedAt(now);
            approvalRequestStepRepository.save(step);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestStep getCurrentPendingStep(String businessType, Long businessId) {
        ApprovalRequest request = getPendingRequest(businessType, businessId);
        return getCurrentStep(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getCurrentApprovers(String businessType, Long businessId) {
        ApprovalRequest request = getPendingRequest(businessType, businessId);
        return approvalRequestStepRepository
                .findByApprovalRequestIdOrderByLevelOrderAsc(request.getId())
                .stream()
                .filter(s -> s.getLevelOrder().equals(request.getCurrentLevel())
                        && s.getStatus() == com.kltn.school_hrm.enums.Enums.ApprovalStatus.PENDING
                        && s.getAssignedApprover() != null)
                .map(ApprovalRequestStep::getAssignedApprover)
                .collect(Collectors.toList());
    }

    private ApprovalRequest getPendingRequest(String businessType, Long businessId) {
        ApprovalRequest request = approvalRequestRepository
                .findByBusinessTypeAndBusinessId(businessType, businessId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy quy trình phê duyệt"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("Yêu cầu phê duyệt không ở trạng thái chờ duyệt (Hiện tại: " + request.getStatus() + ")");
        }
        return request;
    }

    private ApprovalRequestStep getCurrentStep(ApprovalRequest request) {
        return approvalRequestStepRepository
                .findByApprovalRequestIdAndLevelOrder(request.getId(), request.getCurrentLevel())
                .orElseThrow(() -> new BusinessException("Không tìm thấy cấp duyệt hiện tại: " + request.getCurrentLevel()));
    }

    private boolean evaluateCondition(ApprovalLevel level, Map<String, Object> context) {
        if (level.getConditionType() == null || level.getConditionType() == ApprovalConditionType.NONE) {
            return true;
        }

        if (level.getConditionType() == ApprovalConditionType.MIN_LEAVE_DAYS) {
            if (context == null || !context.containsKey("totalDays")) {
                return true;
            }
            Object totalDaysObj = context.get("totalDays");
            BigDecimal totalDays;
            if (totalDaysObj instanceof BigDecimal) {
                totalDays = (BigDecimal) totalDaysObj;
            } else {
                totalDays = new BigDecimal(totalDaysObj.toString());
            }

            BigDecimal threshold = new BigDecimal(level.getConditionValue());
            return totalDays.compareTo(threshold) >= 0;
        }

        return true;
    }

    private String formatConditionSnapshot(ApprovalLevel level) {
        if (level.getConditionType() == null || level.getConditionType() == ApprovalConditionType.NONE) {
            return "ALWAYS";
        }
        return level.getConditionType().name() + " >= " + level.getConditionValue();
    }
}
