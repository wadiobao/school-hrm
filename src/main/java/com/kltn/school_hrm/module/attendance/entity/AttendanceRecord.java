package com.kltn.school_hrm.module.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Business attendance record produced by AttendanceAggregationService.
 * Represents one work day's aggregated check-in/out data.
 * HR/User reads from this entity, not directly from raw logs.
 */
@Entity
@Table(
    name = "attendance_records",
    indexes = {
        @Index(name = "idx_att_record_emp_date", columnList = "employee_id, work_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_att_record_emp_date", columnNames = {"employee_id", "work_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AttendanceRecord extends BaseEntity {

    @Builder.Default
    @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceSession> sessions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    /** Lần check-in đầu tiên trong ngày */
    @Column(name = "first_check_in")
    private LocalDateTime firstCheckIn;

    /** Lần check-out cuối cùng trong ngày */
    @Column(name = "last_check_out")
    private LocalDateTime lastCheckOut;

    /** Số phút đi trễ so với giờ bắt đầu ca */
    @Column(name = "late_minutes")
    private Integer lateMinutes;

    /** Số phút về sớm so với giờ kết thúc ca */
    @Column(name = "early_leave_minutes")
    private Integer earlyLeaveMinutes;

    /** Tổng số phút làm việc thực tế */
    @Column(name = "worked_minutes")
    private Integer workedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttendanceStatus status;

    /** Ghi chú của HR khi điều chỉnh */
    @Column(length = 500)
    private String note;
}
