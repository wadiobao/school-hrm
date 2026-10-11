package com.kltn.school_hrm.module.core.service.implement;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.module.core.entity.Holiday;
import com.kltn.school_hrm.module.core.repository.HolidayRepository;
import com.kltn.school_hrm.module.core.service.WorkingCalendarService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai tính toán ngày làm việc chuẩn (Working Calendar).
 * Quy tắc:
 * 1. Chỉ tính ngày nằm trong khoảng [startDate, endDate], bao gồm cả 2 ngày.
 * 2. Loại trừ thứ Bảy và Chủ nhật.
 * 3. Loại trừ các ngày lễ được khai báo trong HolidayRepository (bao gồm cả ngày lễ lặp lại hàng năm).
 * 4. Không tính trùng ngày nếu ngày lễ rơi vào cuối tuần.
 * 5. Validate startDate <= endDate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCalendarServiceImpl implements WorkingCalendarService {

    private final HolidayRepository holidayRepository;

    @Override
    public long countWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        // Lấy tất cả holiday trong khoảng ngày và tính cả recurring holidays
        Set<LocalDate> holidays = holidayRepository.findByDateBetween(startDate, endDate)
                .stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        // Lấy thêm các holiday dạng lặp lại hàng năm nếu có
        holidayRepository.findAll().stream()
                .filter(h -> Boolean.TRUE.equals(h.getRecurring()) && h.getDate() != null)
                .forEach(h -> {
                    LocalDate recurringDate = h.getDate().withYear(startDate.getYear());
                    if (!recurringDate.isBefore(startDate) && !recurringDate.isAfter(endDate)) {
                        holidays.add(recurringDate);
                    }
                    if (endDate.getYear() > startDate.getYear()) {
                        LocalDate recurringDateNextYear = h.getDate().withYear(endDate.getYear());
                        if (!recurringDateNextYear.isBefore(startDate) && !recurringDateNextYear.isAfter(endDate)) {
                            holidays.add(recurringDateNextYear);
                        }
                    }
                });

        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .filter(date -> !holidays.contains(date))
                .count();
    }

    @Override
    public boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        if (holidayRepository.existsByDate(date)) {
            return false;
        }

        return holidayRepository.findAll().stream()
                .noneMatch(h -> Boolean.TRUE.equals(h.getRecurring())
                        && h.getDate() != null
                        && h.getDate().getMonth() == date.getMonth()
                        && h.getDate().getDayOfMonth() == date.getDayOfMonth());
    }
}
