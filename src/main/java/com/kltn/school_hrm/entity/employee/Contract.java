package com.kltn.school_hrm.entity.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.core.Position;
import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.enums.Enums.ContractType;
import com.kltn.school_hrm.enums.Enums.Currency;
import com.kltn.school_hrm.exception.custom.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
public class Contract extends BaseEntity {

	@Column(name = "contract_number", nullable = false, unique = true, length = 50)
	private String contractNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "position_id", nullable = false)
	private Position position;

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

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ContractType type;

	public void validate() {
		validateDates();
		validateSalary();
	}

	private void validateDates() {

		if (type == ContractType.INDEFINITE_TERM
				&& endDate != null) {
			throw new BusinessException(
					"Indefinite contract cannot have end date");
		}

		if (type != ContractType.INDEFINITE_TERM
				&& endDate == null) {
			throw new BusinessException(
					"Contract requires end date");
		}

		if (endDate != null
				&& endDate.isBefore(startDate)) {
			throw new BusinessException(
					"End date cannot be before start date");
		}
	}

	private void validateSalary() {
		if (grossSalary != null && grossSalary.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException("Gross salary cannot be negative");
		}
		if (housingAllowance != null && housingAllowance.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException("Housing allowance cannot be negative");
		}
		if (flightAllowance != null && flightAllowance.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException("Flight allowance cannot be negative");
		}
		if (relocationAllowance != null && relocationAllowance.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException("Relocation allowance cannot be negative");
		}
	}

	public void terminate() {

		if (status != ContractStatus.ACTIVE) {
			throw new BusinessException(
					"Only active contract can be terminated");
		}

		this.status = ContractStatus.TERMINATED;
	}

	public void expire() {

		if (status != ContractStatus.ACTIVE) {
			throw new BusinessException(
					"Only active contract can expire");
		}

		if (endDate == null) {
			throw new BusinessException(
					"Indefinite contract cannot expire");
		}

		if (LocalDate.now().isBefore(endDate)) {
			throw new BusinessException(
					"Contract has not reached expiration date");
		}

		this.status = ContractStatus.EXPIRED;
	}

	public void changeSalary(BigDecimal newSalary) {

		if (newSalary == null || newSalary.signum() <= 0) {
			throw new IllegalArgumentException(
					"Salary must be greater than 0");
		}

		this.grossSalary = newSalary;
	}
}
