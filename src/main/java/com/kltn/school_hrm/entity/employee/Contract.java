package com.kltn.school_hrm.entity.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import com.kltn.school_hrm.enums.Enums.ContractStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "contract_number", nullable = false, unique = true, length = 50)
	private String contractNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "gross_salary", precision = 15, scale = 2)
	private BigDecimal grossSalary;

	@Column(length = 10)
	private Currency currency; // USD hoặc VND

	@Column(name = "housing_allowance", precision = 15, scale = 2)
	private BigDecimal housingAllowance;

	@Column(name = "flight_allowance", precision = 15, scale = 2)
	private BigDecimal flightAllowance;

	@Column(name = "relocation_allowance", precision = 15, scale = 2)
	private BigDecimal relocationAllowance;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ContractStatus status;
}
