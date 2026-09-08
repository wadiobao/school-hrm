package com.kltn.school_hrm.service.implement;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.entity.attendance.LeaveApproval;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.LeaveApprovalRepository;
import com.kltn.school_hrm.service.LeaveApprovalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveApprovalServiceImpl implements LeaveApprovalService {

    private final LeaveApprovalRepository leaveApprovalRepository;

    @Override
    public void createApprovalSteps(LeaveRequest request) {

        LeaveApproval approver;

        if (request.getApprovals() == null) {
            approver = LeaveApproval.builder()
                    .leaveRequest(request)
                    .approver(request.getEmployee().getDepartment().getManager())
                    .approvalLevel(1)
                    .status(ApprovalStatus.PENDING)
                    .approvedAt(null)
                    .comment(null)
                    .build();

            leaveApprovalRepository.save(approver);
        } else {
            Integer topLevel = request.getApprovals().stream()
                    .map(LeaveApproval::getApprovalLevel)
                    .max(Integer::compare)
                    .get();

            Employee topLevelApprover = request.getApprovals().stream()
                    .filter(ap -> ap.getApprovalLevel().equals(topLevel))
                    .findFirst()
                    .map(LeaveApproval::getApprover)
                    .get();

            approver = LeaveApproval.builder()
                    .leaveRequest(request)
                    .approver(topLevelApprover.getDepartment().getManager())
                    .approvalLevel(topLevel + 1)
                    .status(ApprovalStatus.PENDING)
                    .approvedAt(null)
                    .comment(null)
                    .build();

            leaveApprovalRepository.save(approver);
        }

        request.getApprovals().add(approver);
    }

    @Override
    public void approveCurrentStep(LeaveRequest request, Long approverId, String comment) {
        LeaveApproval step = getCurrentApproval(request);

        if (!step.getApprover().getId().equals(approverId)) {
            throw new BusinessException("Không phải người phê duyệt");
        }

        step.setStatus(ApprovalStatus.APPROVED);
        step.setComment(comment);
        step.setApprovedAt(LocalDateTime.now());

        leaveApprovalRepository.save(step);

    }

    @Override
    public void rejectCurrentStep(LeaveRequest request, Long approverId, String comment) {
        LeaveApproval step = getCurrentApproval(request);

        if (!step.getApprover().getId().equals(approverId)) {
            throw new BusinessException("Không phải người phê duyệt");
        }

        step.setStatus(ApprovalStatus.REJECTED);
        step.setComment(comment);
        step.setApprovedAt(LocalDateTime.now());

        leaveApprovalRepository.save(step);

    }

    @Override
    public boolean isFullyApproved(LeaveRequest request) {
        return request.getApprovals().stream()
                .allMatch(ap -> ap.getStatus() == ApprovalStatus.APPROVED);
    }

    @Override
    public LeaveApproval getCurrentApproval(LeaveRequest request) {
        return request.getApprovals().stream()
                .filter(ap -> ap.getStatus() == ApprovalStatus.PENDING)
                .min(Comparator.comparing(LeaveApproval::getApprovalLevel))
                .orElseThrow(() -> new BusinessException("Đơn nghỉ đã được xử lý hết"));
    }

}
