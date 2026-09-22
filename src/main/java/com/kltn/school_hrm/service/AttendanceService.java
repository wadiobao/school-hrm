package com.kltn.school_hrm.service;

import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.dto.request.AttendanceCorrectionRequest;
import com.kltn.school_hrm.dto.request.CheckInRequest;
import com.kltn.school_hrm.dto.request.CheckOutRequest;
import com.kltn.school_hrm.dto.response.AttendanceResponse;

/**
 * Service quản lý vòng đời chấm công (service.md mục 3.1).
 */
public interface AttendanceService {

    /** Ghi nhận check-in cho nhân viên. */
    AttendanceResponse checkIn(CheckInRequest request);

    /** Ghi nhận check-out cho nhân viên và tự động tính toán kết quả. */
    AttendanceResponse checkOut(CheckOutRequest request);

    /** Hiệu chỉnh thông tin chấm công (dành cho HR/Quản lý). */
    AttendanceResponse correctAttendance(Long id, AttendanceCorrectionRequest request);

    /** Lấy chi tiết chấm công theo ID. */
    AttendanceResponse getAttendanceById(Long id);

    /** Lấy toàn bộ danh sách chấm công. */
    List<AttendanceResponse> getAllAttendances();

    /** Lấy lịch sử chấm công của một nhân viên. */
    List<AttendanceResponse> getAttendancesByEmployeeId(Long employeeId);

    /** Lấy danh sách chấm công trong một ngày làm việc cụ thể. */
    List<AttendanceResponse> getAttendancesByDate(LocalDate workDate);

    /** Xóa bản ghi chấm công. */
    void deleteAttendance(Long id);
}

