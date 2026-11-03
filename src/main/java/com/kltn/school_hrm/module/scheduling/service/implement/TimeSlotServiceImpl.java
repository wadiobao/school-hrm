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

    @Override
    public TimeSlotResponse create(TimeSlotRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

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

        validateTimeRange(request.getStartTime(), request.getEndTime());

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

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessException("Giờ kết thúc tiết học phải sau giờ bắt đầu");
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
