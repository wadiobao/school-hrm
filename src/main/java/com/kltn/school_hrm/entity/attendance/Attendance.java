package com.kltn.school_hrm.entity.attendance;

import java.time.LocalDate;
import java.time.LocalTime;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.AttendanceStatus;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(indexes = { @Index(name = "idx_att_emp_date", columnList = "employee_id, work_date") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Attendance extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "work_date", nullable = false)
	private LocalDate workDate;

	@Column(name = "check_in")
	private LocalTime checkIn;

	@Column(name = "check_out")
	private LocalTime checkOut;

	@Column(name = "device_id", length = 50)
	private String deviceId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private AttendanceStatus status;
}
