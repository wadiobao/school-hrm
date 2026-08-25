package com.kltn.school_hrm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.employee.WorkPermitAndVisa;

@Repository
public interface WorkPermitAndVisaRepository extends JpaRepository<WorkPermitAndVisa, Long> {
	Optional<WorkPermitAndVisa> findByEmployeeId(Long employeeId);

	// Truy vấn các Work Permit / Visa sắp hết hạn trong X ngày (Phục vụ gửi cảnh
	// báo cho HR)
	@Query("SELECT w FROM WorkPermitAndVisa w" +
			" WHERE (w.wpExpiryDate BETWEEN :today AND :warningDate)" +
			" OR (w.trcExpiryDate BETWEEN :today AND :warningDate)")
	List<WorkPermitAndVisa> findExpiringPermitsAndVisas(@Param("today") LocalDate today,
			@Param("warningDate") LocalDate warningDate);
}
