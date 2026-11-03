package com.kltn.school_hrm.module.academic.entity;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;

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
 * Entity quản lý học kỳ thuộc năm học.
 * Đảm bảo thời gian học kỳ nằm trong khoảng của năm học.
 */
@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Semester extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AcademicStatus status;
}

