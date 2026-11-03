package com.kltn.school_hrm.module.academic.entity;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

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
 * Entity quản lý danh mục môn học trong toàn trường.
 */
@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Subject extends BaseEntity {

    @Column(name = "code", length = 30, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SubjectStatus status;
}

