package com.kltn.school_hrm.entity.payroll;

import java.math.BigDecimal;
import java.util.List;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.PayrollStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(indexes = {
		@Index(name = "idx_payroll_emp_month_year", columnList = "employee_id, month, year") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payroll extends BaseEntity {

	@Column(nullable = false)
	private Integer month;

	@Column(nullable = false)
	private Integer year;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "gross_salary", precision = 15, scale = 2)
	private BigDecimal grossSalary;

	@Column(name = "total_work_days", precision = 4, scale = 1)
	private BigDecimal totalWorkDays;

	@Column(name = "total_teaching_hours", precision = 5, scale = 1)
	private BigDecimal totalTeachingHours;

	@Column(name = "total_allowances", precision = 15, scale = 2)
	private BigDecimal totalAllowances;

	@Column(name = "total_deductions", precision = 15, scale = 2)
	private BigDecimal totalDeductions;

	@Column(name = "net_salary", precision = 15, scale = 2)
	private BigDecimal netSalary;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private PayrollStatus status;

	@OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PayrollDetail> details;
}