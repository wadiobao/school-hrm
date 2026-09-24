package com.kltn.school_hrm.entity.approval;

import java.util.ArrayList;
import java.util.List;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Thể hiện một quy trình duyệt cụ thể cho một đối tượng nghiệp vụ (Runtime instance).
 * Có Optimistic Locking (@Version) để bảo vệ tính nhất quán khi duyệt đồng thời.
 */
@Entity
@Table(name = "approval_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ApprovalRequest extends BaseEntity {

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "business_ref_code", length = 100)
    private String businessRefCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private ApprovalPolicy policy;

    @Column(name = "policy_version", nullable = false)
    private Integer policyVersion;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private Integer currentLevel = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Employee requester;

    @Version
    private Long version;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("levelOrder ASC")
    @Builder.Default
    private List<ApprovalRequestStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("actionAt ASC")
    @Builder.Default
    private List<ApprovalAction> actions = new ArrayList<>();
}
