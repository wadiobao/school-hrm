package com.kltn.school_hrm.service.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.enums.Enums.AttendanceStatus;
import com.kltn.school_hrm.service.AttendanceCalculationService;

@Service
public class AttendanceCalculationServiceImpl implements AttendanceCalculationService {

    @Override
    public AttendanceCalculationResult calculate(Attendance attendance, Shift shift) {
        if (attendance == null || shift == null) {
            return AttendanceCalculationResult.builder()
                    .lateMinutes(0)
                    .earlyLeaveMinutes(0)
                    .workedMinutes(0)
                    .status(AttendanceStatus.ABSENT)
                    .build();
        }

        LocalDateTime checkIn = attendance.getCheckIn();
        LocalDateTime checkOut = attendance.getCheckOut();

        // 1. Nếu không có checkIn -> ABSENT
        if (checkIn == null) {
            return AttendanceCalculationResult.builder()
                    .lateMinutes(0)
                    .earlyLeaveMinutes(0)
                    .workedMinutes(0)
                    .status(AttendanceStatus.ABSENT)
                    .build();
        }

        // 2. Xác định thời điểm bắt đầu và kết thúc chuẩn theo ca
        LocalDateTime expectedStart = attendance.getWorkDate().atTime(shift.getStartTime());
        LocalDateTime expectedEnd;

        if (Boolean.TRUE.equals(shift.getOverNight())) {
            // Ca qua đêm -> kết thúc vào ngày hôm sau
            expectedEnd = attendance.getWorkDate().plusDays(1).atTime(shift.getEndTime());
        } else {
            expectedEnd = attendance.getWorkDate().atTime(shift.getEndTime());
        }

        // 3. Tính lateMinutes
        // Mốc ân hạn cho phép
        int graceMinutes = shift.getGraceMinutes() != null ? shift.getGraceMinutes() : 0;
        LocalDateTime graceLimit = expectedStart.plusMinutes(graceMinutes);

        int lateMinutes = 0;
        if (checkIn.isAfter(graceLimit)) {
            // Tính số phút muộn so với mốc bắt đầu chuẩn (trừ số phút ân hạn)
            long diff = Duration.between(expectedStart, checkIn).toMinutes();
            lateMinutes = (int) Math.max(0, diff - graceMinutes);
        }

        // 4. Tính earlyLeaveMinutes & workedMinutes
        int earlyLeaveMinutes = 0;
        int workedMinutes = 0;

        if (checkOut != null) {
            // Nếu check-out trước giờ kết thúc chuẩn của ca -> Tính về sớm
            if (checkOut.isBefore(expectedEnd)) {
                earlyLeaveMinutes = (int) Math.max(0, Duration.between(checkOut, expectedEnd).toMinutes());
            }

            // Tổng thời gian làm việc thực tế
            long totalMinutes = Duration.between(checkIn, checkOut).toMinutes();
            int breakMinutes = shift.getBreakMinutes() != null ? shift.getBreakMinutes() : 0;
            workedMinutes = (int) Math.max(0, totalMinutes - breakMinutes);
        }

        // 5. Xác định AttendanceStatus
        AttendanceStatus status;
        if (lateMinutes > 0 && earlyLeaveMinutes > 0) {
            status = AttendanceStatus.LATE; // Ưu tiên đánh dấu LATE hoặc kết hợp
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
                .workedMinutes(workedMinutes)
                .status(status)
                .build();
    }
}
