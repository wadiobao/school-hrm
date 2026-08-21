package com.kltn.school_hrm.entity.core;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Position extends BaseEntity {

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "position_allowance_rate", precision = 4, scale = 2)
	private BigDecimal positionAllowanceRate;
}