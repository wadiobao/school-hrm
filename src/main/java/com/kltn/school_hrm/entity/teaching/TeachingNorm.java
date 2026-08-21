package com.kltn.school_hrm.entity.teaching;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.core.Position;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class TeachingNorm extends BaseEntity {

	@Column(name = "academic_year", length = 20)
	private String academicYear;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "position_id", nullable = false)
	private Position position;

	@Column(name = "standard_hours")
	private Integer standardHours;

	@Column(name = "reduction_percentage", precision = 5, scale = 2)
	private BigDecimal reductionPercentage;
}