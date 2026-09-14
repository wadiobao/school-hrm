package com.kltn.school_hrm.entity.approval;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.enums.Enums.ApprovalConditionType;
import com.kltn.school_hrm.enums.Enums.ApproverType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Định nghĩa cấu hình cho từng cấp duyệt trong ApprovalPolicy (Configuration).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ApprovalLevel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private ApprovalPolicy policy;

    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", nullable = false, length = 50)
    private ApproverType approverType;

    @Column(name = "approver_role", length = 50)
    private String approverRole;

    @Column(name = "specific_approver_id")
    private Long specificApproverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", length = 50)
    private ApprovalConditionType conditionType;

    @Column(name = "condition_value", length = 100)
    private String conditionValue;
}
