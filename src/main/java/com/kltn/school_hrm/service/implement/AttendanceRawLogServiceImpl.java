package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.AttendanceRawLogRequest;
import com.kltn.school_hrm.dto.response.AttendanceRawLogResponse;
import com.kltn.school_hrm.entity.attendance.AttendanceRawLog;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.AttendanceEventType;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.AttendanceRawLogRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.AttendanceAggregationService;
import com.kltn.school_hrm.service.AttendanceRawLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tiếp nhận raw attendance event, lưu vào DB, sau đó trigger aggregation.
 *
 * Nguyên tắc: Raw log là bất biến - không update/delete sau khi lưu.
 * Chống duplicate qua eventId (DB UNIQUE constraint là lớp bảo vệ cuối).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AttendanceRawLogServiceImpl implements AttendanceRawLogService {

    private static final DateTimeFormatter EVENT_ID_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AttendanceRawLogRepository rawLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceAggregationService aggregationService;

    @Override
    public AttendanceRawLogResponse recordEvent(AttendanceRawLogRequest request) {
        // 1. Validate employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên với ID: " + request.getEmployeeId()));

        LocalDateTime eventTime = request.getEventTime() != null
                ? request.getEventTime()
                : LocalDateTime.now();

        // 2. Sinh eventId nếu client không cung cấp
        String eventId = resolveEventId(request, eventTime);

        // 3. Chống duplicate - kiểm tra eventId đã tồn tại chưa
        if (rawLogRepository.existsByEventId(eventId)) {
            throw new BusinessException("Event đã được ghi nhận trước đó (eventId=" + eventId + "). Bỏ qua duplicate.");
        }

        // 4. Lưu raw log - IMMUTABLE sau khi lưu
        AttendanceRawLog rawLog = AttendanceRawLog.builder()
                .employee(employee)
                .eventTime(eventTime)
                .eventType(request.getEventType())
                .source(request.getSource())
                .deviceId(request.getDeviceId())
                .eventId(eventId)
                .build();

        rawLog = rawLogRepository.save(rawLog);
        log.info("Raw log saved: employee={}, eventType={}, source={}, eventId={}",
                employee.getId(), request.getEventType(), request.getSource(), eventId);

        // 5. Trigger aggregation tự động sau mỗi event OUT
        //    Với event IN chỉ lưu raw log, aggregation chạy khi OUT
        if (request.getEventType() == AttendanceEventType.OUT) {
            LocalDate workDate = eventTime.toLocalDate();
            aggregationService.aggregate(employee.getId(), workDate);
            log.info("Aggregation triggered: employee={}, workDate={}", employee.getId(), workDate);
        }

        return mapToResponse(rawLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRawLogResponse> getLogsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + employeeId);
        }
        // Lấy tất cả logs (90 ngày gần nhất để không quá nặng)
        LocalDateTime from = LocalDateTime.now().minusDays(90);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        return rawLogRepository.findByEmployeeIdAndEventTimeBetween(employeeId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── private helpers ────────────────────────────────────────────────────

    /**
     * Sinh eventId từ source + employeeId + eventTime + UUID short.
     * Format: {SOURCE}-{empId}-{yyyyMMddHHmmss}-{uuid8}
     */
    private String resolveEventId(AttendanceRawLogRequest request, LocalDateTime eventTime) {
        if (request.getEventId() != null && !request.getEventId().isBlank()) {
            return request.getEventId();
        }
        String timeStr = eventTime.format(EVENT_ID_FORMATTER);
        String uuidShort = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return String.format("%s-%s-%s-%s",
                request.getSource().name(),
                request.getEmployeeId(),
                timeStr,
                uuidShort);
    }

    private AttendanceRawLogResponse mapToResponse(AttendanceRawLog log) {
        return AttendanceRawLogResponse.builder()
                .id(log.getId())
                .employeeId(log.getEmployee().getId())
                .employeeName(log.getEmployee().getFullName())
                .eventTime(log.getEventTime())
                .eventType(log.getEventType())
                .source(log.getSource())
                .deviceId(log.getDeviceId())
                .eventId(log.getEventId())
                .build();
    }
}
