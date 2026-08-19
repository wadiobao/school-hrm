package com.kltn.school_hrm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	List<Attendance> findByEmployeeId(Long employeeId);
	List<Attendance> findByWorkDate(LocalDate workDate);
}
