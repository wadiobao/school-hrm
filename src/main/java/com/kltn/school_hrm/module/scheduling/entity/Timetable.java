package com.kltn.school_hrm.module.scheduling.entity;

import java.time.LocalDateTime;

import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;

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
 * Entity lưu một phiên bản thời khóa biểu của một lớp trong một học kỳ.
 * Vòng đời quản lý bởi State Machine: DRAFT -> VALIDATING -> REVIEW -> PUBLISHED -> ARCHIVED.
 */
@Entity
@Table(name = "timetables", uniqueConstraints = {
    @UniqueConstraint(name = "uk_timetable_class_semester_version", columnNames = {"school_class_id", "semester_id", "version"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Timetable extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TimetableStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "validation_notes", columnDefinition = "TEXT")
    private String validationNotes;
}

