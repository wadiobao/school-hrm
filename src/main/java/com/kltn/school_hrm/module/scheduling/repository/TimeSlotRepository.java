package com.kltn.school_hrm.module.scheduling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.scheduling.entity.TimeSlot;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findBySessionOrderByPeriodNumberAsc(TimeSlotSession session);

    List<TimeSlot> findAllByOrderBySessionAscPeriodNumberAsc();

    Optional<TimeSlot> findBySessionAndPeriodNumber(TimeSlotSession session, Integer periodNumber);

    boolean existsBySessionAndPeriodNumber(TimeSlotSession session, Integer periodNumber);

    boolean existsBySessionAndPeriodNumberAndIdNot(TimeSlotSession session, Integer periodNumber, Long id);
}

