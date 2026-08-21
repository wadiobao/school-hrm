package com.kltn.school_hrm.entity.core;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.enums.Enums.RoleCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private RoleCode roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(columnDefinition = "TEXT")
    private String description;
}
