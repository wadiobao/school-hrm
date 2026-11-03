package com.kltn.school_hrm.module.teaching.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.teaching.entity.TeachingAssignmentHistory;

@Repository
public interface TeachingAssignmentHistoryRepository extends JpaRepository<TeachingAssignmentHistory, Long> {

    List<TeachingAssignmentHistory> findByTeachingAssignmentIdOrderByChangedAtDesc(Long teachingAssignmentId);

    List<TeachingAssignmentHistory> findByOldTeacherId(Long oldTeacherId);

    List<TeachingAssignmentHistory> findByNewTeacherId(Long newTeacherId);
}

