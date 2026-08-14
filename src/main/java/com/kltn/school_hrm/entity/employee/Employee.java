package com.kltn.school_hrm.entity.employee;

import java.time.LocalDate;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.core.Department;
import com.kltn.school_hrm.entity.core.Position;
import com.kltn.school_hrm.entity.core.Users;
import com.kltn.school_hrm.enums.Enums.TeacherType;
import com.kltn.school_hrm.enums.status.EmployeeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true)
	private Users user;

	@Column(name = "employee_code", nullable = false, unique = true, length = 20)
	private String employeeCode;

	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;

	@Column(name = "native_name", length = 100)
	private String nativeName;

	@Enumerated(EnumType.STRING)
	@Column(name = "teacher_type", length = 20)
	private TeacherType teacherType;

	@Column(length = 50)
	private String nationality;

	@Column(name = "passport_number", length = 50)
	private String passportNumber;

	@Column(name = "passport_expiry_date")
	private LocalDate passportExpiryDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "position_id", nullable = false)
	private Position position;

	@Column(name = "joined_date")
	private LocalDate joinedDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EmployeeStatus status;
}
