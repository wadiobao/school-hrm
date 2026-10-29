package com.kltn.school_hrm.module.formula.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kltn.school_hrm.module.formula.entity.FormulaDefinition;

public interface FormulaDefinitionRepository extends JpaRepository<FormulaDefinition, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<FormulaDefinition> findByCode(String code);
}
