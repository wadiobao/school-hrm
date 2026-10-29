package com.kltn.school_hrm.module.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.core.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
	Optional<Department> findByCode(String code);

	boolean existsByCode(String code);

	long countByManagerIdAndIdNot(Long managerId, Long departmentId);

	@Query(nativeQuery = true, value = "select d.id ,d.code ,d.\"name\" ,d.parent_id ,d.manager_id , e.full_name as manager_name, pd.\"name\" as parent_name \r\n"
			+ "from demo.departments d \r\n"
			+ "left join demo.departments pd\r\n"
			+ "on d.parent_id = pd.id\r\n"
			+ "left join demo.employees e\r\n"
			+ "on d.manager_id = e.id")
	List<DepartmentWithName> findAllWithManagerAndDepartmentName();

	record DepartmentWithName(Long id, String code, String name, Long parentId, Long managerId, String managerName,
			String parentName) {
	}
}
