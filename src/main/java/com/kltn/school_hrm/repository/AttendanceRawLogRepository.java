package com.kltn.school_hrm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.AttendanceRawLog;
import com.kltn.school_hrm.enums.Enums.AttendanceEventType;

@Repository
public interface AttendanceRawLogRepository extends JpaRepository<AttendanceRawLog, Long> {

    /** Kiểm tra event_id đã tồn tại chưa (chống duplicate) */
    boolean existsByEventId(String eventId);

    /** Lấy tất cả raw logs của một nhân viên trong khoảng thời gian */
    @Query("""
        SELECT r FROM AttendanceRawLog r
        WHERE r.employee.id = :employeeId
          AND r.eventTime >= :from
          AND r.eventTime < :to
        ORDER BY r.eventTime ASC
        """)
    List<AttendanceRawLog> findByEmployeeIdAndEventTimeBetween(
        @Param("employeeId") Long employeeId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    /** Lấy raw log đầu tiên (IN) của nhân viên trong ngày để aggregate */
    @Query("""
        SELECT r FROM AttendanceRawLog r
        WHERE r.employee.id = :employeeId
          AND r.eventTime >= :from
          AND r.eventTime < :to
          AND r.eventType = :eventType
        ORDER BY r.eventTime ASC
        """)
    Optional<AttendanceRawLog> findFirstByEmployeeIdAndEventTimeAndType(
        @Param("employeeId") Long employeeId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("eventType") AttendanceEventType eventType
    );
}
