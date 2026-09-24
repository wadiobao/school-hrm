package com.kltn.school_hrm.entity.attendance;

import java.time.LocalDateTime;

import com.kltn.school_hrm.entity.base.BaseEntity;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.AttendanceEventType;
import com.kltn.school_hrm.enums.Enums.AttendanceSource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Immutable raw attendance event log.
 * Never modified after creation - corrections are handled via AttendanceCorrection.
 * All attendance sources (fingerprint, web, mobile, RFID) converge here.
 */
@Entity
@Table(
    name = "attendance_raw_log",
    indexes = {
        @Index(name = "idx_raw_log_emp_time", columnList = "employee_id, event_time"),
        @Index(name = "idx_raw_log_event_id", columnList = "event_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AttendanceRawLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Thời điểm chấm công thực tế (từ thiết bị hoặc user input) */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /** Loại sự kiện: IN hoặc OUT */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 10)
    private AttendanceEventType eventType;

    /** Nguồn tạo ra event (WEB, MOBILE, FINGERPRINT, ...) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttendanceSource source;

    /** ID thiết bị chấm công - nullable vì WEB/MANUAL không có device cố định */
    @Column(name = "device_id", length = 50)
    private String deviceId;

    /**
     * Unique event ID để chống duplicate khi thiết bị retry.
     * Format: {source}-{employeeId}-{eventTime}-{random}
     * Database UNIQUE constraint là lớp bảo vệ cuối cùng.
     */
    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;
}
