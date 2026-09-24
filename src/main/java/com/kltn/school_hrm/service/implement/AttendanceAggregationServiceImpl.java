package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.AttendanceRawLog;
import com.kltn.school_hrm.entity.attendance.AttendanceRecord;
import com.kltn.school_hrm.entity.attendance.EmployeeShiftAssignment;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.AttendanceEventType;
import com.kltn.school_hrm.enums.Enums.AttendanceStatus;
import com.kltn.school_hrm.repository.AttendanceRawLogRepository;
import com.kltn.school_hrm.repository.AttendanceRecordRepository;
import com.kltn.school_hrm.repository.EmployeeShiftAssignmentRepository;
import com.kltn.school_hrm.service.AttendanceAggregationService;
import com.kltn.school_hrm.service.AttendanceCalculationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gom raw logs của một ngày thành AttendanceRecord.
 * Internal processing - không cần Controller.
 *
 * Logic:
 *   1. Lấy tất cả raw logs của nhân viên trong ngày
 *   2. firstCheckIn = IN event đầu tiên
 *   3. lastCheckOut = OUT event cuối cùng
 *   4. Tạo/cập nhật AttendanceRecord
 *   5. Gọi AttendanceCalculationService để tính late/early/worked/status
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AttendanceAggregationServiceImpl implements AttendanceAggregationService {

    private final AttendanceRawLogRepository rawLogRepository;
    private final AttendanceRecordRepository recordRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final AttendanceCalculationService calculationService;

    @Override
    public AttendanceRecord aggregate(Long employeeId, LocalDate workDate) {
        // 1. Lấy toàn bộ raw logs trong ngày (00:00 → 23:59:59)
        LocalDateTime dayStart = workDate.atStartOfDay();
        LocalDateTime dayEnd = workDate.plusDays(1).atStartOfDay();

        List<AttendanceRawLog> logs = rawLogRepository.findByEmployeeIdAndEventTimeBetween(
                employeeId, dayStart, dayEnd);

        if (logs.isEmpty()) {
            log.debug("Không có raw log nào cho employee={} ngày={}", employeeId, workDate);
            return null;
        }

        // 2. firstCheckIn = IN event có eventTime nhỏ nhất
        LocalDateTime firstCheckIn = logs.stream()
                .filter(l -> l.getEventType() == AttendanceEventType.IN)
                .map(AttendanceRawLog::getEventTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // 3. lastCheckOut = OUT event có eventTime lớn nhất
        LocalDateTime lastCheckOut = logs.stream()
                .filter(l -> l.getEventType() == AttendanceEventType.OUT)
                .map(AttendanceRawLog::getEventTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 4. Tạo hoặc cập nhật AttendanceRecord (idempotent - có thể gọi lại nhiều lần)
        AttendanceRecord record = recordRepository.findByEmployeeIdAndWorkDate(employeeId, workDate)
                .orElseGet(() -> {
                    Employee emp = logs.get(0).getEmployee();
                    return AttendanceRecord.builder()
                            .employee(emp)
                            .workDate(workDate)
                            .build();
                });

        record.setFirstCheckIn(firstCheckIn);
        record.setLastCheckOut(lastCheckOut);

        // 5. Tính toán qua AttendanceCalculationService
        EmployeeShiftAssignment assignment = assignmentRepository.findApplicableAssignment(
                employeeId, workDate.getDayOfWeek(), workDate).orElse(null);

        if (assignment != null && assignment.getShift() != null) {
            Shift shift = assignment.getShift();
            AttendanceCalculationResult result = calculationService.calculateFromRecord(record, shift);
            record.setLateMinutes(result.getLateMinutes());
            record.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
            record.setWorkedMinutes(result.getWorkedMinutes());
            record.setStatus(result.getStatus());
        } else {
            // Không tìm thấy ca - vẫn lưu record nhưng status = PRESENT, không tính late/early
            log.warn("Không tìm thấy ca làm việc cho employee={} ngày={}", employeeId, workDate);
            record.setStatus(AttendanceStatus.PRESENT);
        }

        return recordRepository.save(record);
    }
}
