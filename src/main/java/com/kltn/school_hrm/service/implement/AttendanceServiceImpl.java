package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.AttendanceCorrectionRequest;
import com.kltn.school_hrm.dto.request.CheckInRequest;
import com.kltn.school_hrm.dto.request.CheckOutRequest;
import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.dto.response.AttendanceResponse;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.attendance.EmployeeShiftAssignment;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.AttendanceStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.AttendanceRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.EmployeeShiftAssignmentRepository;
import com.kltn.school_hrm.service.AttendanceCalculationService;
import com.kltn.school_hrm.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final AttendanceCalculationService calculationService;

    @Override
    public AttendanceResponse checkIn(CheckInRequest request) {
        // 1. Employee phải tồn tại
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + request.getEmployeeId()));

        LocalDateTime checkInTime = request.getCheckInTime() != null ? request.getCheckInTime() : LocalDateTime.now();
        LocalDate workDate = checkInTime.toLocalDate();

        // 2. Tìm ca làm việc hợp lệ áp dụng cho nhân viên trong ngày này (service.md)
        EmployeeShiftAssignment assignment = assignmentRepository.findApplicableAssignment(
                employee.getId(),
                workDate.getDayOfWeek(),
                workDate
        ).orElseThrow(() -> new BusinessException("Nhân viên không có lịch phân công ca làm việc nào cho ngày " + workDate));

        Shift shift = assignment.getShift();
        if (!Boolean.TRUE.equals(shift.getIsActive())) {
            throw new BusinessException("Ca làm việc " + shift.getName() + " hiện đang không hoạt động (inactive).");
        }

        // 3. Kiểm tra xem đã tồn tại Attendance trong ngày chưa
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), workDate)
                .orElse(null);

        if (attendance != null && attendance.getCheckIn() != null) {
            throw new BusinessException("Nhân viên đã thực hiện check-in cho ngày " + workDate + " lúc " + attendance.getCheckIn());
        }

        if (attendance == null) {
            attendance = Attendance.builder()
                    .employee(employee)
                    .workDate(workDate)
                    .build();
        }

        // 4. Ghi nhận checkIn và deviceId
        attendance.setCheckIn(checkInTime);
        if (request.getDeviceId() != null) {
            attendance.setDeviceId(request.getDeviceId());
        }

        // 5. Tính toán sơ bộ trạng thái check-in (ví dụ xem có muộn không)
        AttendanceCalculationResult calcResult = calculationService.calculate(attendance, shift);
        attendance.setLateMinutes(calcResult.getLateMinutes());
        attendance.setStatus(calcResult.getStatus());

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    public AttendanceResponse checkOut(CheckOutRequest request) {
        LocalDateTime checkOutTime = request.getCheckOutTime() != null ? request.getCheckOutTime() : LocalDateTime.now();
        LocalDate workDate = request.getWorkDate() != null ? request.getWorkDate() : checkOutTime.toLocalDate();

        // 1. Tìm Attendance trong ngày của nhân viên
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(request.getEmployeeId(), workDate)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin chấm công ngày " + workDate + " để check-out."));

        // 2. Phải có checkIn trước đó
        if (attendance.getCheckIn() == null) {
            throw new BusinessException("Nhân viên chưa thực hiện check-in cho ngày " + workDate);
        }

        // 3. Không được checkout hai lần
        if (attendance.getCheckOut() != null) {
            throw new BusinessException("Nhân viên đã thực hiện check-out trước đó lúc " + attendance.getCheckOut());
        }

        if (checkOutTime.isBefore(attendance.getCheckIn())) {
            throw new BusinessException("Thời gian check-out không thể trước thời gian check-in.");
        }

        // 4. Set checkOut & deviceId
        attendance.setCheckOut(checkOutTime);
        if (request.getDeviceId() != null) {
            attendance.setDeviceId(request.getDeviceId());
        }

        // 5. Lấy ca làm việc và tính toán kết quả chấm công tổng thể qua AttendanceCalculationService
        EmployeeShiftAssignment assignment = assignmentRepository.findApplicableAssignment(
                attendance.getEmployee().getId(),
                workDate.getDayOfWeek(),
                workDate
        ).orElse(null);

        if (assignment != null && assignment.getShift() != null) {
            AttendanceCalculationResult calcResult = calculationService.calculate(attendance, assignment.getShift());
            attendance.setLateMinutes(calcResult.getLateMinutes());
            attendance.setEarlyLeaveMinutes(calcResult.getEarlyLeaveMinutes());
            attendance.setStatus(calcResult.getStatus());
        } else {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    public AttendanceResponse correctAttendance(Long id, AttendanceCorrectionRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi chấm công với ID: " + id));

        if (request.getCheckIn() != null) {
            attendance.setCheckIn(request.getCheckIn());
        }
        if (request.getCheckOut() != null) {
            if (attendance.getCheckIn() != null && request.getCheckOut().isBefore(attendance.getCheckIn())) {
                throw new BusinessException("Thời gian check-out không thể trước thời gian check-in.");
            }
            attendance.setCheckOut(request.getCheckOut());
        }
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getLateMinutes() != null) {
            attendance.setLateMinutes(request.getLateMinutes());
        }
        if (request.getEarlyLeaveMinutes() != null) {
            attendance.setEarlyLeaveMinutes(request.getEarlyLeaveMinutes());
        }

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi chấm công với ID: " + id));
        return mapToResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAllAttendances() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendancesByEmployeeId(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + employeeId);
        }
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendancesByDate(LocalDate workDate) {
        return attendanceRepository.findByWorkDate(workDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bản ghi chấm công với ID: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeCode(attendance.getEmployee().getEmployeeCode())
                .employeeName(attendance.getEmployee().getFullName())
                .workDate(attendance.getWorkDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .deviceId(attendance.getDeviceId())
                .status(attendance.getStatus())
                .lateMinutes(attendance.getLateMinutes())
                .earlyLeaveMinutes(attendance.getEarlyLeaveMinutes())
                .build();
    }
}
