package com.kltn.school_hrm.shared.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.ContractRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.shared.configuration.dto.CustomPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("contractSecurity")
@RequiredArgsConstructor
@Slf4j
public class ContractSecurity {

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;

    /**
     * Kiểm tra quyền xem hợp đồng của một nhân viên cụ thể.
     * SUPER_ADMIN, HR_MANAGER, BOARD_OF_DIRECTORS có quyền xem tất cả.
     * Nhân viên thông thường chỉ có thể xem hợp đồng của chính mình.
     */
    public boolean canViewEmployeeContracts(Long targetEmployeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Quản trị viên và Ban Giám hiệu được xem toàn trường
        if (hasAnyRole(auth, "SUPER_ADMIN", "HR_MANAGER", "BOARD_OF_DIRECTORS")) {
            return true;
        }

        // Kiểm tra xem người dùng hiện tại có phải là chính nhân viên này không
        if (auth.getPrincipal() instanceof CustomPrincipal principal) {
            Long userId = principal.userId();
            return employeeRepository.findById(targetEmployeeId)
                    .map(emp -> emp.getUser() != null && emp.getUser().getId().equals(userId))
                    .orElse(false);
        }

        return false;
    }

    /**
     * Kiểm tra quyền xem một hợp đồng cụ thể theo contractId.
     */
    public boolean canViewContract(Long contractId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        if (hasAnyRole(auth, "SUPER_ADMIN", "HR_MANAGER", "BOARD_OF_DIRECTORS")) {
            return true;
        }

        if (auth.getPrincipal() instanceof CustomPrincipal principal) {
            Long userId = principal.userId();
            return contractRepository.findById(contractId)
                    .map(c -> c.getEmployee().getUser() != null && c.getEmployee().getUser().getId().equals(userId))
                    .orElse(false);
        }

        return false;
    }

    /**
     * Kiểm tra quyền quản lý hợp đồng (Tạo, Gia hạn, Chấm dứt, Xóa).
     * Chỉ SUPER_ADMIN và HR_MANAGER được phép.
     */
    public boolean canManageContracts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return hasAnyRole(auth, "SUPER_ADMIN", "HR_MANAGER");
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
