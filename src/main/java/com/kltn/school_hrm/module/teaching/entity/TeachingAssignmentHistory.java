package com.kltn.school_hrm.module.teaching.entity;

import java.time.LocalDateTime;

import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.shared.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity lưu lịch sử chuyển giao phân công giáo viên giảng dạy.
 * Đảm bảo truy vết lịch sử khi giáo viên nghỉ việc hoặc hoán đổi lớp giữa kỳ.
 */
@Entity
@Table(name = "teaching_assignment_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TeachingAssignmentHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_teacher_id")
    private Employee oldTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_teacher_id", nullable = false)
    private Employee newTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
}

