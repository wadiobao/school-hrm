package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.DepartmentCreateRequest;
import com.kltn.school_hrm.dto.response.DepartmentResponse;
import com.kltn.school_hrm.entity.core.Department;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.repository.DepartmentRepository;
import com.kltn.school_hrm.repository.UserRepository;
import com.kltn.school_hrm.service.DepartmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Department code already exists: " + request.getCode());
        }

        Department department = Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent department not found"));
            department.setParentDepartment(parent);
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager user not found"));
            department.setManager(manager);
        }

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentCreateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (!department.getCode().equals(request.getCode()) && departmentRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Department code already exists: " + request.getCode());
        }

        department.setCode(request.getCode());
        department.setName(request.getName());

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent department not found"));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager user not found"));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return mapToResponse(department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .parentId(department.getParentDepartment() != null ? department.getParentDepartment().getId() : null)
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .build();
    }
}
