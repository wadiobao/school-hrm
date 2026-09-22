package com.kltn.school_hrm.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.kltn.school_hrm.dto.request.AssignShiftRequest;
import com.kltn.school_hrm.dto.request.ChangeShiftRequest;
import com.kltn.school_hrm.dto.request.EndAssignmentRequest;
import com.kltn.school_hrm.dto.response.EmployeeShiftAssignmentResponse;

/**
 * Service quản lý phân công ca làm việc cho nhân viên.
 * Theo kiến trúc dịch vụ chuẩn (service.md).
 */
public interface EmployeeShiftAssignmentService {

    /**
     * Phân công ca làm việc mới cho nhân viên.
     */
    EmployeeShiftAssignmentResponse assign(AssignShiftRequest request);

    /**
     * Thay đổi ca làm việc: Đóng ca cũ và tạo ca mới để bảo toàn lịch sử.
     */
    EmployeeShiftAssignmentResponse changeShift(Long assignmentId, ChangeShiftRequest request);

    /**
     * Đóng / kết thúc một phân ca tại một ngày xác định.
     */
    void endAssignment(Long assignmentId, EndAssignmentRequest request);

    /**
     * Tìm ca làm việc áp dụng cho nhân viên tại một ngày cụ thể (phục vụ check-in).
     */
    Optional<EmployeeShiftAssignmentResponse> findForDate(Long employeeId, LocalDate date);

    /**
     * Lấy lịch làm việc đang hiệu lực của nhân viên.
     */
    List<EmployeeShiftAssignmentResponse> getEmployeeSchedule(Long employeeId);

    /**
     * Lấy tất cả phân ca làm việc trong hệ thống.
     */
    List<EmployeeShiftAssignmentResponse> getAllAssignments();
}
