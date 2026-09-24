package com.kltn.school_hrm.service.implement;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.attendance.AttendanceRecord;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.enums.Enums.AttendanceStatus;
import com.kltn.school_hrm.service.AttendanceCalculationService;

@Service
public class AttendanceCalculationServiceImpl implements AttendanceCalculationService {

    @Override
    public AttendanceCalculationResult calculateFromRecord(AttendanceRecord record, Shift shift) {
        if (record == null || shift == null) {
            return absent();
        }

        LocalDateTime checkIn = record.getFirstCheckIn();
        LocalDateTime checkOut = record.getLastCheckOut();

        if (checkIn == null) {
            return absent();
        }

        return doCalculate(checkIn, checkOut, record.getWorkDate().atTime(shift.getStartTime()),
                buildExpectedEnd(record.getWorkDate(), shift),
                shift.getGraceMinutes(), shift.getBreakMinutes());
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
