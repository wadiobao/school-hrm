package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.ShiftRequest;
import com.kltn.school_hrm.dto.response.ShiftResponse;

public interface ShiftService {

    /** Tạo ca làm việc mới. */
    ShiftResponse createShift(ShiftRequest request);

    /** Cập nhật thông tin ca làm việc. */
    ShiftResponse updateShift(Long id, ShiftRequest request);

    /** Lấy thông tin một ca theo id. */
    ShiftResponse getShiftById(Long id);

    /** Lấy danh sách tất cả ca làm việc. */
    List<ShiftResponse> getAllShifts();

    /** Lấy danh sách ca đang hoạt động. */
    List<ShiftResponse> getActiveShifts();

    /** Xóa ca làm việc (hard delete). */
    void deleteShift(Long id);

    /** Thay đổi trạng thái hoạt động của ca. */
    ShiftResponse toggleActive(Long id);
}
