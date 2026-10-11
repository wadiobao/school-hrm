package com.kltn.school_hrm.module.core.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.core.entity.Holiday;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByDate(LocalDate date);

    List<Holiday> findByDateBetween(LocalDate start, LocalDate end);

    @Query("""
        SELECT h.date
        FROM Holiday h
        WHERE h.date BETWEEN :startDate AND :endDate
    """)
    Set<LocalDate> findDatesBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
