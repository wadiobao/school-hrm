package com.kltn.school_hrm.utils;

import java.util.List;

import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.EmployeeRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DepartmentChartValidationService {

    private final EmployeeRepository employeeRepository;

    /**
     * Hàm chính: Validate mối quan hệ Parent - Child trước khi gán. Quăng ra
     * Exception nếu vi phạm quy tắc.
     * 
     * @param employeeId      ID của nhân viên/sếp cũ
     * @param targetManagerId ID của manager mới
     */
    public void validateParentChildAssignment(Long employeeId, Long targetManagerId) {
        // 1. Gán NULL -> Hợp lệ (Lên làm Root / Chưa có Sếp)
        if (targetManagerId == null) {
            return;
        }

        // 2. Chặn tự gán chính mình làm Sếp (A -> A)
        if (employeeId.equals(targetManagerId)) {
            throw new IllegalArgumentException("Nhân viên không thể tự làm Sếp của chính mình!");
        }

        // 3. Kiểm tra chu trình (LinkedList Cycle Check)
        if (hasCircularDependency(employeeId, targetManagerId)) {
            throw new IllegalStateException(
                    String.format("Không thể gán! Manager (ID: %d) đang thuộc nhánh cấp dưới của nhân viên (ID: %d).",
                            targetManagerId, employeeId));
        }
    }

    /**
     * Thuật toán duyệt LinkedList: Leo ngược từ targetManagerId lên Root. Nếu đụng
     * lại employeeId -> Trả về true (Bị dính chu trình).
     */
    public boolean hasCircularDependency(Long employeeId, Long targetManagerId) {
        Long checkManagerId = targetManagerId;

        while (checkManagerId != null) {
            // Phát hiện điểm lặp
            if (checkManagerId.equals(employeeId)) {
                return true;
            }

            // Leo lên Sếp cấp cao hơn
            Employee checkEmployee = employeeRepository.findById(checkManagerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id "));
            checkManagerId = checkEmployee.getDepartment().getManager().getId();
        }

        return false; // An toàn
    }
}
