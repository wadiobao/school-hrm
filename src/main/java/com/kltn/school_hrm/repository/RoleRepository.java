package com.kltn.school_hrm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.core.Role;
import com.kltn.school_hrm.enums.Enums.RoleCode;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleCode(RoleCode roleCode);
}
