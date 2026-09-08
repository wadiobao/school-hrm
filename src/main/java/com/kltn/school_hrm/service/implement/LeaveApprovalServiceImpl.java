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
        if (request.getApprovals() == null) {
            request.setApprovals(new java.util.ArrayList<>());
        }

        Employee employee = request.getEmployee();
        if (employee.getDepartment() == null || employee.getDepartment().getManager() == null) {
            throw new BusinessException("Nhân viên chưa có phòng ban hoặc trưởng phòng ban");
        }

        // Cấp 1: Quản lý trực tiếp (Department Manager)
        LeaveApproval manager = LeaveApproval.builder()
                .leaveRequest(request)
                .approver(employee.getDepartment().getManager())
                .approvalLevel(1)
                .status(ApprovalStatus.PENDING)
                .build();
        leaveApprovalRepository.save(manager);
        request.getApprovals().add(manager);

        // Nếu nghỉ từ 2 ngày trở lên: Cần thêm cấp 2 (Hiệu trưởng / Parent Department Manager)
        if (request.getTotalDays() != null && request.getTotalDays().compareTo(java.math.BigDecimal.valueOf(2)) >= 0) {
            if (employee.getDepartment().getParentDepartment() != null
                    && employee.getDepartment().getParentDepartment().getManager() != null) {
                LeaveApproval principal = LeaveApproval.builder()
                        .leaveRequest(request)
                        .approver(employee.getDepartment().getParentDepartment().getManager())
                        .approvalLevel(2)
                        .status(ApprovalStatus.PENDING)
                        .build();
                leaveApprovalRepository.save(principal);
                request.getApprovals().add(principal);
            }
        }
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
