package com.kltn.school_hrm.module.scheduling.service.implement;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.scheduling.dto.request.TimeSlotRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimeSlotResponse;
import com.kltn.school_hrm.module.scheduling.entity.TimeSlot;
import com.kltn.school_hrm.module.scheduling.repository.TimeSlotRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.scheduling.service.TimeSlotService;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý khung giờ tiết học trong ngày.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    @Override
    public TimeSlotResponse create(TimeSlotRequest request) {
        // SCH-06: Kiểm tra tính hợp lệ và chồng lấn khung giờ
        validateTimeRangeAndOverlap(request.getSession(), request.getStartTime(), request.getEndTime(), null);

        if (timeSlotRepository.existsBySessionAndPeriodNumber(request.getSession(), request.getPeriodNumber())) {
            throw new BusinessException(String.format(
                    "Tiết %d trong buổi %s đã tồn tại",
                    request.getPeriodNumber(), request.getSession().getLabel()));
        }

        TimeSlot entity = TimeSlot.builder()
                .periodNumber(request.getPeriodNumber())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .session(request.getSession())
                .build();

        return mapToResponse(timeSlotRepository.save(entity));
    }

    @Override
    public TimeSlotResponse update(Long id, TimeSlotRequest request) {
        TimeSlot entity = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + id));

        // SCH-06: Kiểm tra tiết học có đang sử dụng trong TKB đã công bố không
        long publishedUsages = timetableEntryRepository.countEntriesByTimeSlotIdInPublishedTimetable(id);
        if (publishedUsages > 0) {
            throw new BusinessException("Không thể điều chỉnh tiết học đang được sử dụng trong thời khóa biểu chính thức (PUBLISHED)");
        }

        // SCH-06: Kiểm tra tính hợp lệ và chồng lấn khung giờ
        validateTimeRangeAndOverlap(request.getSession(), request.getStartTime(), request.getEndTime(), id);

        if (timeSlotRepository.existsBySessionAndPeriodNumberAndIdNot(request.getSession(), request.getPeriodNumber(), id)) {
            throw new BusinessException(String.format(
                    "Tiết %d trong buổi %s đã tồn tại",
                    request.getPeriodNumber(), request.getSession().getLabel()));
        }

        entity.setPeriodNumber(request.getPeriodNumber());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setSession(request.getSession());

        return mapToResponse(timeSlotRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TimeSlotResponse getById(Long id) {
        return timeSlotRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAll() {
        return timeSlotRepository.findAllByOrderBySessionAscPeriodNumberAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getBySession(TimeSlotSession session) {
        return timeSlotRepository.findBySessionOrderByPeriodNumberAsc(session).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy tiết học với id: " + id);
        }

        // SCH-06: Không được xóa TimeSlot đang dùng trong TKB đã công bố
        long publishedUsages = timetableEntryRepository.countEntriesByTimeSlotIdInPublishedTimetable(id);
        if (publishedUsages > 0) {
            throw new BusinessException("Không thể xóa tiết học đang được sử dụng trong thời khóa biểu chính thức (PUBLISHED)");
        }

        timeSlotRepository.deleteById(id);
    }

    @Override
    public void initDefaultSlotsIfEmpty() {
        if (timeSlotRepository.count() == 0) {
            // Khởi tạo các tiết học tiêu chuẩn mẫu (Sáng 5 tiết, Chiều 4 tiết)
            LocalTime[][] morningSlots = {
                {LocalTime.of(7, 0), LocalTime.of(7, 45)},
                {LocalTime.of(7, 50), LocalTime.of(8, 35)},
                {LocalTime.of(8, 50), LocalTime.of(9, 35)},
                {LocalTime.of(9, 40), LocalTime.of(10, 25)},
                {LocalTime.of(10, 30), LocalTime.of(11, 15)}
            };
            for (int i = 0; i < morningSlots.length; i++) {
                timeSlotRepository.save(TimeSlot.builder()
                        .periodNumber(i + 1)
                        .startTime(morningSlots[i][0])
                        .endTime(morningSlots[i][1])
                        .session(TimeSlotSession.MORNING)
                        .build());
            }

            LocalTime[][] afternoonSlots = {
                {LocalTime.of(13, 0), LocalTime.of(13, 45)},
                {LocalTime.of(13, 50), LocalTime.of(14, 35)},
                {LocalTime.of(14, 50), LocalTime.of(15, 35)},
                {LocalTime.of(15, 40), LocalTime.of(16, 25)}
            };
            for (int i = 0; i < afternoonSlots.length; i++) {
                timeSlotRepository.save(TimeSlot.builder()
                        .periodNumber(i + 1)
                        .startTime(afternoonSlots[i][0])
                        .endTime(afternoonSlots[i][1])
                        .session(TimeSlotSession.AFTERNOON)
                        .build());
            }
        }
    }

    /**
     * BR-13 & SCH-06: Kiểm tra giờ kết thúc sau giờ bắt đầu và không bị chồng lấn khung giờ với tiết khác trong cùng buổi.
     * Công thức chồng lấn: newStart < existingEnd && newEnd > existingStart.
     */
    private void validateTimeRangeAndOverlap(TimeSlotSession session, LocalTime start, LocalTime end, Long excludeId) {
        if (start == null || end == null) {
            throw new BusinessException("Thời gian bắt đầu và kết thúc tiết học không được để trống");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("Giờ kết thúc tiết học phải sau giờ bắt đầu");
        }

        List<TimeSlot> existingSlots = timeSlotRepository.findBySessionOrderByPeriodNumberAsc(session);
        for (TimeSlot existing : existingSlots) {
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }
            if (existing.getStartTime() == null || existing.getEndTime() == null) {
                continue;
            }

            // Kiểm tra chồng lấn thời gian
            boolean isOverlapping = start.isBefore(existing.getEndTime()) && end.isAfter(existing.getStartTime());
            if (isOverlapping) {
                throw new BusinessException(String.format(
                        "Khung giờ %s - %s bị chồng lấn với Tiết %d (%s - %s) trong buổi %s",
                        start, end, existing.getPeriodNumber(),
                        existing.getStartTime(), existing.getEndTime(), session.getLabel()));
            }
        }
    }

    private TimeSlotResponse mapToResponse(TimeSlot entity) {
        return TimeSlotResponse.builder()
                .id(entity.getId())
                .periodNumber(entity.getPeriodNumber())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .session(entity.getSession())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

