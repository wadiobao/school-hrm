package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

}
