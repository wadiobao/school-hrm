package com.kltn.school_hrm.utils;

import java.util.Set;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.entity.attendance.Holiday;
import com.kltn.school_hrm.repository.HolidayRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LeaveDayCalculator {
    private final HolidayRepository holidayRepository;

    public int calculate(LocalDate startDate, LocalDate endDate) {

        // Lấy danh sách các ngày lễ trong khoảng thời gian
        Set<LocalDate> holidays = holidayRepository
                .findByDateBetween(startDate, endDate)
                .stream()
                .map(t -> {
                    LocalDate date = t.getDate();
                    if (Boolean.TRUE.equals(t.getRecurring())) {
                        int year = startDate.getYear();
                        date = date.withYear(year);
                    }
                    return date;
                })
                .collect(Collectors.toSet());

        int totalDays = 0;

        LocalDate current = startDate;

        // Duyệt qua từng ngày từ startDate đến endDate
        while (!current.isAfter(endDate)) {

            // Nếu ngày hiện tại không phải là cuối tuần và không phải là ngày lễ thì tăng số ngày nghỉ
            if (!isWeekend(current)
                    && !holidays.contains(current)) {

                totalDays++;
            }

            current = current.plusDays(1);
        }

        return totalDays;
    }

    // Kiểm tra xem ngày hiện tại có phải là cuối tuần không
    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();

        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }
}
