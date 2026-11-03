package com.kltn.school_hrm.module.academic.entity;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity quản lý năm học trong hệ thống.
 * Chứa khung thời gian bắt đầu - kết thúc và trạng thái năm học.
 */
@Entity
@Table(name = "academic_years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AcademicYear extends BaseEntity {

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AcademicStatus status;
}

