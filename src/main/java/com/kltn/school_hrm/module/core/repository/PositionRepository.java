package com.kltn.school_hrm.module.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.core.entity.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
	java.util.Optional<Position> findByCode(String code);
	boolean existsByCode(String code);
}
