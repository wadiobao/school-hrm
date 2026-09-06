package com.kltn.school_hrm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByDate(LocalDate date);

    List<Holiday> findByDateBetween(LocalDate start, LocalDate end);
}
