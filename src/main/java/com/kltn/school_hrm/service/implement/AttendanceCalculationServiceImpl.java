package com.kltn.school_hrm.service.implement;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.attendance.AttendanceRecord;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;
import com.kltn.school_hrm.service.AttendanceCalculationService;

@Service
public class AttendanceCalculationServiceImpl implements AttendanceCalculationService {

    @Override
    public AttendanceCalculationResult calculateFromRecord(AttendanceRecord record, Shift shift) {
        if (record == null || shift == null || record.getSessions() == null || record.getSessions().isEmpty()) {
            return absent();
        }

        LocalDateTime expectedStart = record.getWorkDate().atTime(shift.getStartTime());
        LocalDateTime expectedEnd = buildExpectedEnd(record.getWorkDate(), shift);
        int graceMinutes = shift.getGraceMinutes() != null ? shift.getGraceMinutes() : 0;
        LocalDateTime graceLimit = expectedStart.plusMinutes(graceMinutes);

        LocalDateTime firstCheckIn = record.getFirstCheckIn();
        LocalDateTime lastCheckOut = record.getLastCheckOut();

        if (firstCheckIn == null) {
            return absent();
        }

        // Tính lateMinutes (Cách 2: tính từ giờ bắt đầu ca, không trừ grace period)
        // Ví dụ: ca 08:00, grace=5 phút, check-in 08:06 → lateMinutes = 6 phút
        int lateMinutes = 0;
        if (firstCheckIn.isAfter(graceLimit)) {
            lateMinutes = (int) Duration.between(expectedStart, firstCheckIn).toMinutes();
        }

        // INCOMPLETE: có IN nhưng không có OUT → không thể tính thời gian làm việc
        if (lastCheckOut == null) {
            return AttendanceCalculationResult.builder()
                    .lateMinutes(lateMinutes)
                    .earlyLeaveMinutes(0)
                    .workedMinutes(0)
                    .status(AttendanceStatus.INCOMPLETE)
                    .build();
        }

        // Tính earlyLeaveMinutes (dùng chung graceMinutes)
        // Ví dụ: endTime=17:00, grace=5 → về trước 16:55 mới bị tính
        LocalDateTime earlyLeaveLimit = expectedEnd.minusMinutes(graceMinutes);
        int earlyLeaveMinutes = 0;
        if (lastCheckOut.isBefore(earlyLeaveLimit)) {
            earlyLeaveMinutes = (int) Duration.between(lastCheckOut, expectedEnd).toMinutes();
        }

        // Tính workedMinutes tổng hợp từ các sessions
        int totalWorkedMinutes = 0;
        for (com.kltn.school_hrm.entity.attendance.AttendanceSession session : record.getSessions()) {
            if (session.getWorkedMinutes() != null) {
                totalWorkedMinutes += session.getWorkedMinutes();
            }
        }

        // Nếu chỉ có 1 session (không quẹt thẻ ra/vào giữa giờ), tự động trừ giờ nghỉ trưa (nếu có)
        if (record.getSessions().size() == 1) {
            int breakMinutes = shift.getBreakMinutes() != null ? shift.getBreakMinutes() : 0;
            totalWorkedMinutes = Math.max(0, totalWorkedMinutes - breakMinutes);
        }

        // Xác định AttendanceStatus
        AttendanceStatus status;
        if (lateMinutes > 0 && earlyLeaveMinutes > 0) {
            status = AttendanceStatus.LATE; // muộn được ưu tiên hiển thị
        } else if (lateMinutes > 0) {
            status = AttendanceStatus.LATE;
        } else if (earlyLeaveMinutes > 0) {
            status = AttendanceStatus.EARLY_LEAVE;
        } else {
            status = AttendanceStatus.PRESENT;
        }

        return AttendanceCalculationResult.builder()
                .lateMinutes(lateMinutes)
                .earlyLeaveMinutes(earlyLeaveMinutes)
                .workedMinutes(totalWorkedMinutes)
                .status(status)
                .build();
    }

    @Override
    @Deprecated
    public AttendanceCalculationResult calculate(Attendance attendance, Shift shift) {
        if (attendance == null || shift == null) {
            return absent();
        }

        LocalDateTime checkIn = attendance.getCheckIn();
        LocalDateTime checkOut = attendance.getCheckOut();

        if (checkIn == null) {
            return absent();
        }

        return doCalculate(checkIn, checkOut,
                attendance.getWorkDate().atTime(shift.getStartTime()),
                buildExpectedEnd(attendance.getWorkDate(), shift),
                shift.getGraceMinutes(), shift.getBreakMinutes());
    }

    // ─── private helpers ────────────────────────────────────────────────────

    private AttendanceCalculationResult doCalculate(
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            LocalDateTime expectedStart,
            LocalDateTime expectedEnd,
            Integer graceMinutesCfg,
            Integer breakMinutesCfg) {

        int graceMinutes = graceMinutesCfg != null ? graceMinutesCfg : 0;
        LocalDateTime graceLimit = expectedStart.plusMinutes(graceMinutes);

        // Tính lateMinutes
        int lateMinutes = 0;
        if (checkIn.isAfter(graceLimit)) {
            long diff = Duration.between(expectedStart, checkIn).toMinutes();
            lateMinutes = (int) Math.max(0, diff - graceMinutes);
        }

        // Tính earlyLeaveMinutes & workedMinutes
        int earlyLeaveMinutes = 0;
        int workedMinutes = 0;

        if (checkOut != null) {
            if (checkOut.isBefore(expectedEnd)) {
                earlyLeaveMinutes = (int) Math.max(0, Duration.between(checkOut, expectedEnd).toMinutes());
            }
            int breakMinutes = breakMinutesCfg != null ? breakMinutesCfg : 0;
            workedMinutes = (int) Math.max(0, Duration.between(checkIn, checkOut).toMinutes() - breakMinutes);
        }

        // Xác định AttendanceStatus
        AttendanceStatus status;
        if (lateMinutes > 0) {
            status = AttendanceStatus.LATE;
        } else if (earlyLeaveMinutes > 0) {
            status = AttendanceStatus.EARLY_LEAVE;
        } else {
            status = AttendanceStatus.PRESENT;
        }

        return AttendanceCalculationResult.builder()
                .lateMinutes(lateMinutes)
                .earlyLeaveMinutes(earlyLeaveMinutes)
                .workedMinutes(workedMinutes)
                .status(status)
                .build();
    }

    private java.time.LocalDateTime buildExpectedEnd(java.time.LocalDate workDate, Shift shift) {
        if (Boolean.TRUE.equals(shift.getOverNight())) {
            return workDate.plusDays(1).atTime(shift.getEndTime());
        }
        return workDate.atTime(shift.getEndTime());
    }

    private AttendanceCalculationResult absent() {
        return AttendanceCalculationResult.builder()
                .lateMinutes(0)
                .earlyLeaveMinutes(0)
                .workedMinutes(0)
                .status(AttendanceStatus.ABSENT)
                .build();
    }
}
