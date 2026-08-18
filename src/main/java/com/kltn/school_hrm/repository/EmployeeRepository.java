package com.kltn.school_hrm.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.employee.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	Optional<Employee> findByEmployeeCode(String employeeCode);

	boolean existsByEmployeeCode(String employeeCode);

	boolean existsByEmail(String email);

	// Tìm kiếm nhân viên theo phòng ban và từ khóa (tên/mã)
	@Query("SELECT e FROM Employee e WHERE " +
			"(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
			"(:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<Employee> searchEmployees(@Param("departmentId") Long departmentId,
			@Param("keyword") String keyword,
			Pageable pageable);
}
