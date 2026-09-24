package com.kltn.school_hrm.entity.approval;

import java.time.LocalDateTime;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ApprovalActionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Lịch sử vết từng hành động phê duyệt, từ chối, chuyển tiếp, hoặc hủy (Audit Trail).
 */
@Entity
@Table(name = "approval_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ApprovalAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    /**
     * Bước duyệt tương ứng (có thể null nếu là action SUBMIT hoặc CANCEL tổng thể).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_step_id")
    private ApprovalRequestStep approvalRequestStep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private Employee actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalActionType action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "action_at", nullable = false)
    @Builder.Default
    private LocalDateTime actionAt = LocalDateTime.now();
}
