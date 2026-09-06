package com.kltn.school_hrm.entity.attendance;

import java.time.LocalDate;

import com.kltn.school_hrm.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday extends BaseEntity {

    @Column(length = 50)
    private String name;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    private Boolean recurring;

    private Integer year;
}
