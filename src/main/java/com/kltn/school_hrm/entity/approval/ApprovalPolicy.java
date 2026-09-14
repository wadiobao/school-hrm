package com.kltn.school_hrm.entity.approval;

import java.util.ArrayList;
import java.util.List;

import com.kltn.school_hrm.entity.base.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Định nghĩa quy trình phê duyệt cho một nghiệp vụ (Policy configuration).
 * Policy có tính bất biến theo từng version: khi cập nhật cấu hình duyệt,
 * hệ thống sẽ sinh version mới và đặt version mới nhất là active.
 */
@Entity
@Table(name = "approval_policy", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code", "version"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ApprovalPolicy extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("levelOrder ASC")
    @Builder.Default
    private List<ApprovalLevel> levels = new ArrayList<>();
}
