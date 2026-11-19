package com.kltn.school_hrm.shared.utils;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.shared.configuration.dto.CustomPrincipal;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Component kiểm tra phân quyền cho phân hệ Xếp thời khóa biểu & Phân công giảng dạy.
 * Quy tắc nghiệp vụ:
 * 1. Chỉ có Trưởng ban nhân sự (HEAD_OF_HR / HR_MANAGER) hoặc Ban Giám hiệu / Phó hiệu trưởng mới có quyền tạo và chỉnh sửa.
 * 2. Xem TKB toàn trường: Hiệu trưởng, Phó hiệu trưởng, HR, Trưởng bộ môn.
 * 3. Xem TKB cá nhân: Giáo viên thông thường chỉ xem lịch giảng dạy của chính mình.
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

    /**
     * Kiểm tra người dùng có quyền xem thời khóa biểu của tất cả giáo viên hay không.
     * Hiệu trưởng, HR, Trưởng bộ môn có quyền xem toàn bộ. Giáo viên thông thường chỉ xem cá nhân.
     */
    public boolean canViewAllTeacherSchedules() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // 1. Roles: SUPER_ADMIN, HR_MANAGER, BOARD_OF_DIRECTORS, HEAD_OF_DEPARTMENT
        if (hasAnyRole(auth, "SUPER_ADMIN", "HR_MANAGER", "BOARD_OF_DIRECTORS", "HEAD_OF_DEPARTMENT")) {
            return true;
        }

        // 2. Positions: HEAD_OF_HR, VICE_PRINCIPAL, PRINCIPAL, HEAD_OF_DEPARTMENT
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
                                || posCode == PositionCode.PRINCIPAL
                                || posCode == PositionCode.HEAD_OF_DEPARTMENT;
                    })
                    .orElse(false);
        }

        return false;
    }

    /**
     * Lấy thực thể Employee tương ứng với tài khoản người dùng đang đăng nhập.
     */
    public Optional<Employee> getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomPrincipal principal) {
            return employeeRepository.findByUserId(principal.userId());
        }
        return Optional.empty();
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

