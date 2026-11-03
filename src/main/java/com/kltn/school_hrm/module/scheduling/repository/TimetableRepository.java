package com.kltn.school_hrm.module.scheduling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findBySchoolClassIdAndSemesterIdOrderByVersionDesc(Long schoolClassId, Long semesterId);

    Optional<Timetable> findBySchoolClassIdAndSemesterIdAndStatus(Long schoolClassId, Long semesterId, TimetableStatus status);

    List<Timetable> findBySemesterIdAndStatus(Long semesterId, TimetableStatus status);

    @Query("SELECT COALESCE(MAX(t.version), 0) FROM Timetable t WHERE t.schoolClass.id = :classId AND t.semester.id = :semesterId")
    Integer findMaxVersionByClassAndSemester(@Param("classId") Long classId, @Param("semesterId") Long semesterId);

    boolean existsBySchoolClassIdAndSemesterIdAndVersion(Long schoolClassId, Long semesterId, Integer version);
}

