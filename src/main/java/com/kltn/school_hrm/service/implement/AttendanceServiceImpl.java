package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.AttendanceCreateRequest;
import com.kltn.school_hrm.dto.response.AttendanceResponse;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.repository.AttendanceRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public AttendanceResponse createAttendance(AttendanceCreateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .workDate(request.getWorkDate())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .deviceId(request.getDeviceId())
                .status(request.getStatus())
                .build();

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceCreateRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        attendance.setEmployee(employee);
        attendance.setWorkDate(request.getWorkDate());
        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setDeviceId(request.getDeviceId());
        attendance.setStatus(request.getStatus());

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
        return mapToResponse(attendance);
    }

    @Override
    public List<AttendanceResponse> getAllAttendances() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getAttendancesByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getAttendancesByDate(LocalDate workDate) {
        return attendanceRepository.findByWorkDate(workDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new RuntimeException("Attendance not found");
        }
        attendanceRepository.deleteById(id);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .workDate(attendance.getWorkDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .deviceId(attendance.getDeviceId())
                .status(attendance.getStatus())
                .build();
    }
}
