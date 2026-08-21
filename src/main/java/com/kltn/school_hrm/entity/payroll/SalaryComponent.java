package com.kltn.school_hrm.entity.payroll;


import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.enums.Enums.ComponentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class SalaryComponent extends BaseEntity {

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ComponentType type;

	@Column(name = "is_taxable")
	private Boolean isTaxable;

	@Column(name = "formula_expression", columnDefinition = "TEXT")
	private String formulaExpression;
}
