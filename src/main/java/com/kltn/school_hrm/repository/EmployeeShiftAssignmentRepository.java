package com.kltn.school_hrm.repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.EmployeeShiftAssignment;

@Repository
public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {

    /**
     * Tìm phân công ca áp dụng cho một nhân viên tại một ngày cụ thể (service.md).
     */
    @Query("""
        SELECT esa FROM EmployeeShiftAssignment esa
        JOIN FETCH esa.shift s
        JOIN FETCH esa.employee e
        WHERE esa.active = true
          AND e.id = :employeeId
          AND esa.dayOfWeek = :dayOfWeek
          AND esa.effectiveFrom <= :date
          AND (esa.effectiveTo IS NULL OR esa.effectiveTo >= :date)
    """)
    Optional<EmployeeShiftAssignment> findApplicableAssignment(
        @Param("employeeId") Long employeeId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("date") LocalDate date
    );

    /**
     * Kiểm tra xem có phân công ca nào bị trùng lặp thời gian hay không (req.md rule 8).
     * Bị overlap nếu:
     * (newFrom <= existingTo OR existingTo IS NULL) AND (newTo >= existingFrom OR newTo IS NULL)
     */
    @Query("""
        SELECT COUNT(esa) > 0 FROM EmployeeShiftAssignment esa
        WHERE esa.active = true
          AND esa.employee.id = :employeeId
          AND esa.dayOfWeek = :dayOfWeek
          AND (:excludeId IS NULL OR esa.id != :excludeId)
          AND (esa.effectiveTo IS NULL OR :newFrom <= esa.effectiveTo)
          AND (:newTo IS NULL OR esa.effectiveFrom <= :newTo)
    """)
    boolean existsOverlappingAssignment(
        @Param("employeeId") Long employeeId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("newFrom") LocalDate newFrom,
        @Param("newTo") LocalDate newTo,
        @Param("excludeId") Long excludeId
    );

    /**
     * Lấy danh sách phân ca còn hiệu lực (active) của một nhân viên.
     */
    @Query("""
        SELECT esa FROM EmployeeShiftAssignment esa
        JOIN FETCH esa.shift s
        JOIN FETCH esa.employee e
        WHERE esa.active = true
          AND e.id = :employeeId
        ORDER BY esa.dayOfWeek ASC, esa.effectiveFrom DESC
    """)
    List<EmployeeShiftAssignment> findActiveScheduleByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Lấy toàn bộ lịch sử phân ca của một nhân viên.
     */
    @Query("""
        SELECT esa FROM EmployeeShiftAssignment esa
        JOIN FETCH esa.shift s
        JOIN FETCH esa.employee e
        WHERE e.id = :employeeId
        ORDER BY esa.effectiveFrom DESC
    """)
    List<EmployeeShiftAssignment> findAllByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Lấy danh sách tất cả phân ca cùng Shift & Employee.
     */
    @Query("""
        SELECT esa FROM EmployeeShiftAssignment esa
        JOIN FETCH esa.shift s
        JOIN FETCH esa.employee e
        ORDER BY esa.id DESC
    """)
    List<EmployeeShiftAssignment> findAllWithDetails();

    /**
     * Kiểm tra xem Shift đã được sử dụng trong phân công nào hay chưa.
     */
    boolean existsByShiftId(Long shiftId);
}
