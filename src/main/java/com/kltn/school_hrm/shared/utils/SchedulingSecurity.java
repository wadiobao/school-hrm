package com.kltn.school_hrm.shared.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.shared.configuration.dto.CustomPrincipal;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Component kiểm tra phân quyền cho phân hệ Xếp thời khóa biểu & Phân công giảng dạy.
 * Quy tắc nghiệp vụ: Chỉ có Trưởng ban nhân sự (HEAD_OF_HR / HR_MANAGER)
 * hoặc Phó hiệu trưởng (VICE_PRINCIPAL / BOARD_OF_DIRECTORS) mới có quyền tạo và chỉnh sửa.
 */
@Component("schedulingSecurity")
@RequiredArgsConstructor
@Slf4j
public class SchedulingSecurity {

    private final EmployeeRepository employeeRepository;

    /**
     * Kiểm tra quyền tạo / chỉnh sửa / công bố thời khóa biểu và phân công giảng dạy.
     */
    public boolean canManageSchedule() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // 1. Kiểm tra qua Role quản trị
        if (hasAnyRole(auth, "SUPER_ADMIN", "HR_MANAGER", "BOARD_OF_DIRECTORS")) {
            return true;
        }

        // 2. Kiểm tra qua Chức vụ (Position) của Nhân viên gắn với User
        if (auth.getPrincipal() instanceof CustomPrincipal principal) {
            Long userId = principal.userId();
            return employeeRepository.findByUserId(userId)
                    .map(emp -> {
                        if (emp.getPosition() == null || emp.getPosition().getCode() == null) {
                            return false;
                        }
                        PositionCode posCode = emp.getPosition().getCode();
                        return posCode == PositionCode.HEAD_OF_HR
                                || posCode == PositionCode.VICE_PRINCIPAL
                                || posCode == PositionCode.PRINCIPAL;
                    })
                    .orElse(false);
        }

        return false;
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority();
                    for (String role : roles) {
                        if (authority.equals(role) || authority.equals("ROLE_" + role)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}

