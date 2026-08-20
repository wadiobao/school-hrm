package com.kltn.school_hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.teaching.TeachingNorm;

@Repository
public interface TeachingNormRepository extends JpaRepository<TeachingNorm, Long> {

    List<TeachingNorm> findByAcademicYear(String academicYear);
    List<TeachingNorm> findByPositionId(Long positionId);
    Optional<TeachingNorm> findByPositionIdAndAcademicYear(Long positionId, String academicYear);
}
