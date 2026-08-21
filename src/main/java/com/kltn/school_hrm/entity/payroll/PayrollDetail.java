package com.kltn.school_hrm.entity.payroll;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.base.BaseEntity;

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
public class PayrollDetail extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payroll_id", nullable = false)
	private Payroll payroll;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "component_id", nullable = false)
	private SalaryComponent component;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;
}