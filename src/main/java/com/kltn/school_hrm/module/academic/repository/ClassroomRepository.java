package com.kltn.school_hrm.module.academic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Classroom> findByStatus(ClassroomStatus status);

    List<Classroom> findByRoomType(ClassroomType roomType);
}

