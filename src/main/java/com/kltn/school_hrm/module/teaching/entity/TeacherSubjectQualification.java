package com.kltn.school_hrm.module.teaching.entity;

import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

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
 * Entity lưu chuyên môn sư phạm và môn học giáo viên đủ điều kiện giảng dạy.
 * Ràng buộc: Mỗi giáo viên chỉ có 1 bản ghi trình độ cho 1 môn học.
 */
@Entity
@Table(name = "teacher_subject_qualifications", uniqueConstraints = {
    @UniqueConstraint(name = "uk_teacher_subject_qualification", columnNames = {"teacher_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TeacherSubjectQualification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Employee teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "qualification_level", length = 50)
    private String qualificationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SubjectStatus status;
}

