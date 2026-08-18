package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.EmployeeCreateRequest;
import com.kltn.school_hrm.dto.request.WorkPermitRequest;
import com.kltn.school_hrm.dto.response.EmployeeResponse;
import com.kltn.school_hrm.dto.response.WorkPermitResponse;
import com.kltn.school_hrm.entity.core.Department;
import com.kltn.school_hrm.entity.core.Position;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.entity.employee.WorkPermitAndVisa;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.DepartmentRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.PositionRepository;
import com.kltn.school_hrm.repository.UserRepository;
import com.kltn.school_hrm.repository.WorkPermitAndVisaRepository;
import com.kltn.school_hrm.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final PositionRepository positionRepository;
	private final UserRepository userRepository;
	private final WorkPermitAndVisaRepository workPermitAndVisaRepository;

	@Override
	@Transactional
	public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
		if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
			throw new IllegalArgumentException("Mã nhân viên đã tồn tại!");
		}
		if (employeeRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email đã tồn tại!");
		}

		Department department = departmentRepository.findById(request.getDepartmentId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Không tìm thấy phòng ban với id: " + request.getDepartmentId()));

		Position position = positionRepository.findById(request.getPositionId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Không tìm thấy chức vụ với id: " + request.getPositionId()));

		User user = null;
		if (request.getUserId() != null) {
			user = userRepository.findById(request.getUserId())
					.orElseThrow(
							() -> new ResourceNotFoundException("Không tìm thấy user với id: " + request.getUserId()));
		}

		Employee employee = Employee.builder()
				.employeeCode(request.getEmployeeCode())
				.fullName(request.getFullName())
				.nativeName(request.getNativeName())
				.dateOfBirth(request.getDateOfBirth())
				.gender(request.getGender())
				.email(request.getEmail())
				.phone(request.getPhone())
				.address(request.getAddress())
				.department(department)
				.position(position)
				.status(EmployeeStatus.WORKING)
				.joinDate(request.getJoinDate())
				.user(user)
				.build();

		Employee savedEmployee = employeeRepository.save(employee);
		return mapToResponse(savedEmployee);
	}

	@Override
	@Transactional(readOnly = true)
	public EmployeeResponse getEmployeeById(Long id) {
		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + id));
		return mapToResponse(employee);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EmployeeResponse> searchEmployees(Long departmentId, String keyword, Pageable pageable) {
		return employeeRepository.searchEmployees(departmentId, keyword, pageable)
				.map(this::mapToResponse);
	}

	@Override
	@Transactional
	public WorkPermitResponse updateWorkPermit(Long employeeId, WorkPermitRequest request) {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + employeeId));

		WorkPermitAndVisa permit = workPermitAndVisaRepository.findByEmployeeId(employeeId)
				.orElseGet(() -> WorkPermitAndVisa.builder().employee(employee).build());

		permit.setWorkPermitNumber(request.getWorkPermitNumber());
		permit.setWpIssueDate(request.getWpIssueDate());
		permit.setWpExpiryDate(request.getWpExpiryDate());
		permit.setWpStatus(request.getWpStatus());
		permit.setTrcNumber(request.getTrcNumber());
		permit.setTrcExpiryDate(request.getTrcExpiryDate());
		permit.setDocumentScanUrl(request.getDocumentScanUrl());

		WorkPermitAndVisa saved = workPermitAndVisaRepository.save(permit);
		return mapToWorkPermitResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkPermitResponse> getExpiringWorkPermitsAndVisas(int withinDays) {
		LocalDate today = LocalDate.now();
		LocalDate warningDate = today.plusDays(withinDays);

		return workPermitAndVisaRepository.findExpiringPermitsAndVisas(today, warningDate).stream()
				.map(this::mapToWorkPermitResponse)
				.collect(Collectors.toList());
	}

	private EmployeeResponse mapToResponse(Employee employee) {
		WorkPermitResponse workPermitResp = null;
		if (employee.getWorkPermitAndVisa() != null) {
			workPermitResp = mapToWorkPermitResponse(employee.getWorkPermitAndVisa());
		}

		return EmployeeResponse.builder()
				.id(employee.getId())
				.employeeCode(employee.getEmployeeCode())
				.fullName(employee.getFullName())
				.nativeName(employee.getNativeName())
				.dateOfBirth(employee.getDateOfBirth())
				.gender(employee.getGender())
				.email(employee.getEmail())
				.phone(employee.getPhone())
				.address(employee.getAddress())
				.departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
				.positionName(employee.getPosition() != null ? employee.getPosition().getName() : null)
				.status(employee.getStatus())
				.joinDate(employee.getJoinDate())
				.workPermitAndVisa(workPermitResp)
				.build();
	}

	private WorkPermitResponse mapToWorkPermitResponse(WorkPermitAndVisa permit) {
		return WorkPermitResponse.builder()
				.id(permit.getId())
				.workPermitNumber(permit.getWorkPermitNumber())
				.wpIssueDate(permit.getWpIssueDate())
				.wpExpiryDate(permit.getWpExpiryDate())
				.wpStatus(permit.getWpStatus())
				.trcNumber(permit.getTrcNumber())
				.trcExpiryDate(permit.getTrcExpiryDate())
				.documentScanUrl(permit.getDocumentScanUrl())
				.build();
	}
}
