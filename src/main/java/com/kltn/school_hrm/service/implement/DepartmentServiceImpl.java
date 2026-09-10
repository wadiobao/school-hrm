package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.DepartmentCreateRequest;
import com.kltn.school_hrm.dto.response.DepartmentResponse;
import com.kltn.school_hrm.entity.core.Department;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.repository.DepartmentRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.DepartmentService;
import com.kltn.school_hrm.utils.DepartmentChartValidationService;

import com.kltn.school_hrm.entity.core.Role;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.enums.Enums.RoleCode;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.LeaveApprovalRepository;
import com.kltn.school_hrm.repository.RoleRepository;
import com.kltn.school_hrm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LeaveApprovalRepository leaveApprovalRepository;
    private final DepartmentChartValidationService departmentChartValidationService;

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

        department.setManager(null);

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

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartmentManager(Long departmentId, Long employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban với id: " + departmentId));

        Employee oldManager = department.getManager();

        // 1. Nếu không có gì thay đổi
        if (oldManager != null && oldManager.getId().equals(employeeId)) {
            return mapToResponse(department);
        }
        if (oldManager == null && employeeId == null) {
            return mapToResponse(department);
        }

        // 2. Thu hồi quyền quản lý của sếp cũ (nếu có và không còn quản lý phòng ban khác)
        if (oldManager != null) {
            demoteEmployeeUserRole(oldManager, departmentId);
        }

        // 3. Trường hợp BÃI NHIỆM (employeeId == null)
        if (employeeId == null) {
            department.setManager(null);
        } else {
            // 4. Trường hợp BỔ NHIỆM / THAY THẾ (employeeId != null)
            Employee newManager = validateAndGetManager(employeeId);

            // Kiểm tra vòng lặp nếu trước đó đã có sếp cũ
            if (oldManager != null) {
                departmentChartValidationService.validateParentChildAssignment(oldManager.getId(), employeeId);
            }

            department.setManager(newManager);
            promoteEmployeeUserRole(newManager);

            // Đồng bộ phòng ban làm việc của Manager về chính phòng ban đó
            if (newManager.getDepartment() == null || !newManager.getDepartment().getId().equals(department.getId())) {
                newManager.setDepartment(department);
                employeeRepository.save(newManager);
            }

            // Chuyển giao các đơn nghỉ phép PENDING từ sếp cũ sang sếp mới
            if (oldManager != null) {
                leaveApprovalRepository.transferPendingApprovals(oldManager.getId(), newManager.getId());
            }
        }

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    private Employee validateAndGetManager(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + employeeId));

        if (employee.getStatus() != null && employee.getStatus() != EmployeeStatus.WORKING) {
            throw new BusinessException(
                    "Nhân viên phải đang ở trạng thái 'Đang làm việc' (WORKING) mới có thể đảm nhận vị trí quản lý");
        }

        return employee;
    }

    private void promoteEmployeeUserRole(Employee manager) {
        if (manager.getUser() != null) {
            User user = manager.getUser();
            // Nếu user chưa phải là SUPER_ADMIN hoặc BOARD_OF_DIRECTORS, nâng quyền lên HEAD_OF_DEPARTMENT
            if (user.getRole() == null ||
                    (user.getRole().getRoleCode() != RoleCode.SUPER_ADMIN &&
                            user.getRole().getRoleCode() != RoleCode.BOARD_OF_DIRECTORS)) {
                roleRepository.findByRoleCode(RoleCode.HEAD_OF_DEPARTMENT).ifPresent(role -> {
                    user.setRole(role);
                    userRepository.save(user);
                });
            }
        }
    }

    private void demoteEmployeeUserRole(Employee oldManager, Long currentDepartmentId) {
        // Chỉ hạ quyền nếu nhân viên không còn quản lý phòng ban nào khác
        long otherDepartmentsManaged = departmentRepository.countByManagerIdAndIdNot(oldManager.getId(), currentDepartmentId);
        if (otherDepartmentsManaged == 0 && oldManager.getUser() != null) {
            User user = oldManager.getUser();
            // Chỉ hạ quyền nếu role hiện tại là HEAD_OF_DEPARTMENT
            if (user.getRole() != null && user.getRole().getRoleCode() == RoleCode.HEAD_OF_DEPARTMENT) {
                RoleCode fallbackRole = (oldManager.getTeacherType() != null) ? RoleCode.TEACHER : RoleCode.STAFF;
                roleRepository.findByRoleCode(fallbackRole).ifPresent(role -> {
                    user.setRole(role);
                    userRepository.save(user);
                });
            }
        }
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
