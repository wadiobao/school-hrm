package com.kltn.school_hrm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.AttendanceRecord;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<AttendanceRecord> findByEmployeeId(Long employeeId);

    List<AttendanceRecord> findByWorkDate(LocalDate workDate);

    @Query("""
        SELECT r FROM AttendanceRecord r
        WHERE r.employee.id = :employeeId
          AND r.workDate >= :from
          AND r.workDate <= :to
        ORDER BY r.workDate ASC
        """)
    List<AttendanceRecord> findByEmployeeIdAndWorkDateBetween(
        @Param("employeeId") Long employeeId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("""
        SELECT r FROM AttendanceRecord r
        WHERE r.workDate >= :from
          AND r.workDate <= :to
        ORDER BY r.employee.id ASC, r.workDate ASC
        """)
    List<AttendanceRecord> findByWorkDateBetween(
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
