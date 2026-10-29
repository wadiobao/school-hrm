package com.kltn.school_hrm.module.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.core.entity.Position;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
	Optional<Position> findByCode(PositionCode code);

	boolean existsByCode(PositionCode code);
}
