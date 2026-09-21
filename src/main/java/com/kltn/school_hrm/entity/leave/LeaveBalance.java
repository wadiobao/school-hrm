package com.kltn.school_hrm.entity.leave;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.LeaveType;

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
public class LeaveBalance extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "year", nullable = false)
	private Integer year;

	@Column(name = "total_days", nullable = false)
	private BigDecimal totalDays;

	@Column(name = "used_days", nullable = false)
	private BigDecimal usedDays;

	@Column(name = "pending_days", nullable = false)
	private BigDecimal pendingDays;

	@Column(name = "expired_days", nullable = false)
	private BigDecimal expiredDays;

	public BigDecimal getRemainingDays() {
		return totalDays.subtract(usedDays).subtract(pendingDays).subtract(expiredDays);
	}

	public void expire(BigDecimal days) {
		this.expiredDays = this.expiredDays.add(days);
	}
}
