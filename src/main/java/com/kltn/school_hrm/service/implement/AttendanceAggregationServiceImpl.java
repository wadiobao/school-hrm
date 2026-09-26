package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.AttendanceRawLog;
import com.kltn.school_hrm.entity.attendance.AttendanceRecord;
import com.kltn.school_hrm.entity.attendance.AttendanceSession;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Gom raw logs của một ngày thành AttendanceRecord.
 * Internal processing - không cần Controller.
 *
 * Logic:
 *   1. Lấy tất cả raw logs của nhân viên trong ngày
 *   2. Loại bỏ event dư thừa, ghép cặp IN-OUT tạo các AttendanceSession
 *   3. Gắn sessions vào AttendanceRecord
 *   4. Gọi AttendanceCalculationService để tính toán
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
        LocalDateTime dayStart = workDate.atStartOfDay();
        LocalDateTime dayEnd = workDate.plusDays(1).atStartOfDay();

        List<AttendanceRawLog> logs = rawLogRepository.findByEmployeeIdAndEventTimeBetween(
                employeeId, dayStart, dayEnd);

        if (logs.isEmpty()) {
            log.debug("Không có raw log nào cho employee={} ngày={}", employeeId, workDate);
            return null;
        }

        // Sắp xếp logs theo thời gian
        logs.sort(Comparator.comparing(AttendanceRawLog::getEventTime));

        // Lấy record hiện tại hoặc tạo mới
        AttendanceRecord record = recordRepository.findByEmployeeIdAndWorkDate(employeeId, workDate)
                .orElseGet(() -> {
                    Employee emp = logs.get(0).getEmployee();
                    return AttendanceRecord.builder()
                            .employee(emp)
                            .workDate(workDate)
                            .sessions(new ArrayList<>())
                            .build();
                });

        // Xóa sessions cũ (nếu có) để tính lại từ đầu
        if (record.getSessions() != null) {
            record.getSessions().clear();
        } else {
            record.setSessions(new ArrayList<>());
        }

        LocalDateTime currentIn = null;
        LocalDateTime currentOut = null;
        List<AttendanceSession> sessions = new ArrayList<>();

        for (AttendanceRawLog log : logs) {
            if (log.getEventType() == AttendanceEventType.IN) {
                if (currentIn == null) {
                    currentIn = log.getEventTime();
                    currentOut = null;
                } else {
                    if (currentOut != null) {
                        // Đã có 1 cặp IN-OUT hoàn chỉnh, lưu lại trước khi bắt đầu cặp mới
                        sessions.add(createSession(record, currentIn, currentOut));
                        currentIn = log.getEventTime();
                        currentOut = null;
                    }
                    // Nếu currentOut == null -> Duplicate IN -> bỏ qua theo policy "chỉ giữ IN đầu tiên"
                }
            } else if (log.getEventType() == AttendanceEventType.OUT) {
                if (currentIn != null) {
                    // Cập nhật OUT mới nhất (nếu có duplicate OUT, sẽ giữ OUT cuối cùng)
                    currentOut = log.getEventTime();
                }
                // Nếu currentIn == null -> OUT mồ côi -> bỏ qua
            }
        }

        // Lưu phiên làm việc cuối cùng (nếu có)
        if (currentIn != null) {
            sessions.add(createSession(record, currentIn, currentOut));
        }

        record.getSessions().addAll(sessions);

        // Cập nhật firstCheckIn và lastCheckOut tổng của ngày
        record.setFirstCheckIn(sessions.isEmpty() ? null : sessions.get(0).getCheckIn());
        
        LocalDateTime lastCheckOutTotal = null;
        for (AttendanceSession s : sessions) {
            if (s.getCheckOut() != null) {
                lastCheckOutTotal = s.getCheckOut();
            }
        }
        record.setLastCheckOut(lastCheckOutTotal);

        // Tính toán
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
            // Không có lịch -> status là PRESENT vì họ có quẹt thẻ
            record.setStatus(AttendanceStatus.PRESENT);
            record.setWorkedMinutes(sessions.stream()
                .mapToInt(s -> s.getWorkedMinutes() != null ? s.getWorkedMinutes() : 0)
                .sum());
        }

        return recordRepository.save(record);
    }

    private AttendanceSession createSession(
            AttendanceRecord record, LocalDateTime checkIn, LocalDateTime checkOut) {
        
        Integer workedMinutes = 0;
        if (checkIn != null && checkOut != null) {
            workedMinutes = (int) Duration.between(checkIn, checkOut).toMinutes();
        }

        return AttendanceSession.builder()
                .attendanceRecord(record)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .workedMinutes(workedMinutes)
                .build();
    }
}
