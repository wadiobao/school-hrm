package com.kltn.school_hrm.service.approval.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.entity.approval.ApprovalLevel;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.RoleCode;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.approval.ApproverResolverService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApproverResolverServiceImpl implements ApproverResolverService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee resolveApprover(ApprovalLevel level, Employee requester) {
        if (level == null || level.getApproverType() == null) {
            throw new BusinessException("Cấu hình cấp duyệt không hợp lệ");
        }

        switch (level.getApproverType()) {
            case DIRECT_MANAGER:
                if (requester.getDepartment() == null || requester.getDepartment().getManager() == null) {
                    throw new BusinessException("Nhân viên chưa có phòng ban hoặc phòng ban chưa được bổ nhiệm quản lý");
                }
                return requester.getDepartment().getManager();

            case PARENT_DEPARTMENT_MANAGER:
                if (requester.getDepartment() == null || requester.getDepartment().getParentDepartment() == null
                        || requester.getDepartment().getParentDepartment().getManager() == null) {
                    // Fallback: Nếu không có Parent Department Manager, thử tìm người có Role BOARD_OF_DIRECTORS
                    List<Employee> board = employeeRepository.findByRoleCode(RoleCode.BOARD_OF_DIRECTORS);
                    if (!board.isEmpty()) {
                        return board.get(0);
                    }
                    throw new BusinessException("Không tìm thấy Quản lý cấp trên hoặc Ban Giám hiệu để duyệt");
                }
                return requester.getDepartment().getParentDepartment().getManager();

            case SPECIFIC_ROLE:
                if (level.getApproverRole() == null || level.getApproverRole().isBlank()) {
                    throw new BusinessException("Chưa chỉ định vai trò cho cấp duyệt: " + level.getLevelName());
                }
                RoleCode roleCode = RoleCode.valueOf(level.getApproverRole());
                List<Employee> approversByRole = employeeRepository.findByRoleCode(roleCode);
                if (approversByRole.isEmpty()) {
                    throw new BusinessException("Không tìm thấy nhân viên nào có vai trò " + roleCode.getLabel() + " để duyệt");
                }
                return approversByRole.get(0);

            case SPECIFIC_EMPLOYEE:
                if (level.getSpecificApproverId() == null) {
                    throw new BusinessException("Chưa chỉ định nhân sự cụ thể cho cấp duyệt: " + level.getLevelName());
                }
                return employeeRepository.findById(level.getSpecificApproverId())
                        .orElseThrow(() -> new BusinessException("Không tìm thấy nhân sự được chỉ định duyệt"));

            default:
                throw new BusinessException("Loại người duyệt chưa được hỗ trợ: " + level.getApproverType());
        }
    }
}
