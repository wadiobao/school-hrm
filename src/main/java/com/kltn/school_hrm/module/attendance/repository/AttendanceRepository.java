package com.kltn.school_hrm.module.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.attendance.entity.Attendance;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeId(Long employeeId);

    List<Attendance> findByWorkDate(LocalDate workDate);

    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("""
        SELECT a FROM Attendance a
        JOIN FETCH a.employee e
        WHERE a.workDate = :workDate
    """)
    List<Attendance> findAllByWorkDateWithEmployee(@Param("workDate") LocalDate workDate);

    /** Lấy tất cả bản ghi chấm công của nhân viên trong khoảng ngày (dùng tính lương) */
    List<Attendance> findByEmployeeIdAndWorkDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    /** Đếm ngày PRESENT hoặc LATE hoặc EARLY_LEAVE trong khỳ */
    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.employee.id = :employeeId
          AND a.workDate BETWEEN :startDate AND :endDate
          AND a.status IN :statuses
    """)
    long countByEmployeeAndDateRangeAndStatuses(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AttendanceStatus> statuses);
}

