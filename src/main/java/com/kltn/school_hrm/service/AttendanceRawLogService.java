package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.AttendanceRawLogRequest;
import com.kltn.school_hrm.dto.response.AttendanceRawLogResponse;

/**
 * Tiếp nhận và lưu raw attendance events từ mọi nguồn.
 * Controller chỉ gọi service này, không xử lý logic.
 * Raw log là bất biến - không có update/delete.
 */
public interface AttendanceRawLogService {

    /**
     * Ghi nhận một raw attendance event.
     * Tự động trigger aggregation nếu là event OUT.
     * Chống duplicate bằng eventId.
     *
     * @param request raw event từ bất kỳ nguồn nào
     * @return response với thông tin event đã lưu
     */
    AttendanceRawLogResponse recordEvent(AttendanceRawLogRequest request);

    /**
     * Lấy tất cả raw logs của nhân viên (dành cho debug/audit).
     *
     * @param employeeId ID nhân viên
     * @return danh sách raw logs
     */
    List<AttendanceRawLogResponse> getLogsByEmployee(Long employeeId);
}
