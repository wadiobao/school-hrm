package com.kltn.school_hrm.entity.teaching;

import java.time.LocalDate;

import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.TeachingLogType;
import com.kltn.school_hrm.enums.status.RequestStatus;

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
public class TeachingLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id", nullable = false)
	private TeachingAssignment assignment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actual_teacher_id", nullable = false)
	private Employee actualTeacher;

	@Column(name = "teaching_date", nullable = false)
	private LocalDate teachingDate;

	@Column(name = "periods_taught", nullable = false)
	private Integer periodsTaught;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private TeachingLogType type;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private RequestStatus status;
}