package com.kltn.school_hrm.module.teaching.entity;

import java.time.LocalDateTime;

import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity phân công giáo viên giảng dạy cho lớp và môn học trong một năm học.
 * Ràng buộc: Mỗi lớp - môn học chỉ có 1 giáo viên phụ trách chính trong 1 năm học.
 */
@Entity
@Table(name = "teaching_assignments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_teaching_assignment_class_subject", columnNames = {"school_class_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TeachingAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee teacher;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Curriculum curriculum; // IB, CAMBRIDGE, AP, MOET...

    @Column(name = "subject_name", length = 150)
    private String subjectName;

    @Column(name = "grade_level", length = 20)
    private String gradeLevel;

    @Column(name = "weekly_contact_hours")
    private Double weeklyContactHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TeachingAssignmentStatus status;
}
