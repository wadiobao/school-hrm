package com.kltn.school_hrm.module.academic.entity;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entity quản lý lớp học thuộc một năm học cụ thể.
 * Chứa thông tin khối lớp, giáo viên chủ nhiệm và sĩ số tối đa.
 */
@Entity
@Table(name = "school_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SchoolClass extends BaseEntity {

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homeroom_teacher_id")
    private Employee homeroomTeacher;

    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ClassStatus status;
}

