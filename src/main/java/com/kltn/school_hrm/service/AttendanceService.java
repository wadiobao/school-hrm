package com.kltn.school_hrm.service;

import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.dto.request.AttendanceCreateRequest;
import com.kltn.school_hrm.dto.response.AttendanceResponse;

public interface AttendanceService {
    AttendanceResponse createAttendance(AttendanceCreateRequest request);
    AttendanceResponse updateAttendance(Long id, AttendanceCreateRequest request);
    AttendanceResponse getAttendanceById(Long id);
    List<AttendanceResponse> getAllAttendances();
    List<AttendanceResponse> getAttendancesByEmployeeId(Long employeeId);
    List<AttendanceResponse> getAttendancesByDate(LocalDate workDate);
    void deleteAttendance(Long id);
}
