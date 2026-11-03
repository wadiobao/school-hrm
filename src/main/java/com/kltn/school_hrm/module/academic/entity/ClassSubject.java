package com.kltn.school_hrm.module.academic.entity;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entity cấu hình chương trình học của một lớp trong một học kỳ.
 * Xác định lớp đó học môn gì và số tiết yêu cầu mỗi tuần.
 */
@Entity
@Table(name = "class_subjects", uniqueConstraints = {
    @UniqueConstraint(name = "uk_class_subject_semester", columnNames = {"school_class_id", "subject_id", "semester_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClassSubject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "periods_per_week", nullable = false)
    private Integer periodsPerWeek;
}

