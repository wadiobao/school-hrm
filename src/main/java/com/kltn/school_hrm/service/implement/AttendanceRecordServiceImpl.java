package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.response.AttendanceRecordResponse;
import com.kltn.school_hrm.entity.attendance.AttendanceRecord;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.AttendanceRecordRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.AttendanceAggregationService;
import com.kltn.school_hrm.service.AttendanceRecordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private final AttendanceRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceAggregationService aggregationService;

    @Override
    @Transactional(readOnly = true)
    public AttendanceRecordResponse getById(Long id) {
        return mapToResponse(recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy attendance record ID: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employeeId);
        }
        return recordRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getByDateRange(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        return recordRepository.findByWorkDateBetween(from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employeeId);
        }
        validateDateRange(from, to);
        return recordRepository.findByEmployeeIdAndWorkDateBetween(employeeId, from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceRecordResponse reAggregate(Long employeeId, LocalDate workDate) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employeeId);
        }
        AttendanceRecord record = aggregationService.aggregate(employeeId, workDate);
        if (record == null) {
            throw new BusinessException("Không có raw log nào để aggregate cho employee="
                    + employeeId + " ngày=" + workDate);
        }
        return mapToResponse(record);
    }

    // ─── mapper ────────────────────────────────────────────────────────────

    private AttendanceRecordResponse mapToResponse(AttendanceRecord record) {
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .employeeId(record.getEmployee().getId())
                .employeeCode(record.getEmployee().getEmployeeCode())
                .employeeName(record.getEmployee().getFullName())
                .workDate(record.getWorkDate())
                .firstCheckIn(record.getFirstCheckIn())
                .lastCheckOut(record.getLastCheckOut())
                .lateMinutes(record.getLateMinutes())
                .earlyLeaveMinutes(record.getEarlyLeaveMinutes())
                .workedMinutes(record.getWorkedMinutes())
                .status(record.getStatus())
                .note(record.getNote())
                .build();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }
    }
}
