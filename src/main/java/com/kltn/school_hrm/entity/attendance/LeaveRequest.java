package com.kltn.school_hrm.entity.attendance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.enums.Enums.LeaveType;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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
public class LeaveRequest extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Enumerated(EnumType.STRING)
	@Column(name = "leave_type", length = 30)
	private LeaveType leaveType;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	/** Số ngày nghỉ thực tế (loại trừ cuối tuần và ngày lễ). Bước tối thiểu: 0.5 */
	@Column(name = "total_days", nullable = false, precision = 5, scale = 1)
	private BigDecimal totalDays;

	@Column(columnDefinition = "TEXT")
	private String reason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "substitute_teacher_id")
	private Employee substituteTeacher;

	@OneToMany(mappedBy = "leaveRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
	private List<LeaveApproval> approvals;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private RequestStatus status;
}
