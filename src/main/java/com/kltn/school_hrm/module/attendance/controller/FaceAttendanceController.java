package com.kltn.school_hrm.module.attendance.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.attendance.dto.request.AttendanceRawLogRequest;
import com.kltn.school_hrm.module.attendance.dto.request.FaceScanRequest;
import com.kltn.school_hrm.module.attendance.dto.response.FaceScanResponse;
import com.kltn.school_hrm.module.attendance.entity.Attendance;
import com.kltn.school_hrm.module.attendance.entity.AttendanceRawLog;
import com.kltn.school_hrm.module.attendance.entity.AttendanceRecord;
import com.kltn.school_hrm.module.attendance.repository.AttendanceRawLogRepository;
import com.kltn.school_hrm.module.attendance.repository.AttendanceRecordRepository;
import com.kltn.school_hrm.module.attendance.repository.AttendanceRepository;
import com.kltn.school_hrm.module.attendance.service.AttendanceRawLogService;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceEventType;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceSource;
import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller kết nối Camera AI nhận diện khuôn mặt (cam-simulation/camera_client.py & Web Camera Simulator)
 * với hệ thống Chấm công (AttendanceRawLog + AttendanceRecord + Attendance).
 */
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Slf4j
public class FaceAttendanceController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long COOLDOWN_SECONDS = 30L;

    /**
     * Bảng ánh xạ mã ảnh cũ trong dataset (vd: ANH001.jpg) sang mã nhân viên chuẩn trong DB.
     */
    private static final Map<String, String> DATASET_CODE_ALIASES = Map.of(
            "ANH001", "EMP-GV-001",
            "ANH002", "EMP-TBT-001",
            "ANH003", "EMP-VAN-T01",
            "GV001", "EMP-GV-001"
    );

    private final EmployeeRepository employeeRepository;
    private final AttendanceRawLogRepository rawLogRepository;
    private final AttendanceRawLogService rawLogService;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceRepository legacyAttendanceRepository;

    /**
     * Endpoint nhận kết quả quét khuôn mặt từ camera_client.py hoặc Web Simulator.
     * Tương thích trực tiếp với cấu trúc JSON của camera_client.py:
     * Request: {"employee_code": "EMP-GV-001"}
     * Response: {"status": "SUCCESS", "type": "IN", "time": "07:05:12", ...}
     */
    @PostMapping("/scan-face")
    @Transactional
    public ResponseEntity<FaceScanResponse> scanFace(@Valid @RequestBody FaceScanRequest request) {
        String rawCode = request.getEmployeeCode().trim().toUpperCase();
        String resolvedCode = DATASET_CODE_ALIASES.getOrDefault(rawCode, rawCode);

        Optional<Employee> empOpt = employeeRepository.findByEmployeeCode(resolvedCode);
        if (empOpt.isEmpty() && !rawCode.equals(resolvedCode)) {
            empOpt = employeeRepository.findByEmployeeCode(rawCode);
        }
        // Fallback tìm theo hậu tố hoặc lấy nhân viên đầu tiên nếu user đặt tên ảnh chưa khớp
        if (empOpt.isEmpty()) {
            empOpt = employeeRepository.findAll().stream()
                    .filter(e -> e.getEmployeeCode() != null
                            && (e.getEmployeeCode().equalsIgnoreCase(rawCode)
                                || e.getEmployeeCode().replace("-", "").equalsIgnoreCase(rawCode.replace("-", ""))))
                    .findFirst();
        }

        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(FaceScanResponse.builder()
                    .status("ERROR")
                    .employeeCode(rawCode)
                    .message("Không tìm thấy nhân sự với mã khuôn mặt: " + rawCode)
                    .build());
        }

        Employee employee = empOpt.get();
        LocalDateTime eventTime = request.getSimulatedTime() != null
                ? request.getSimulatedTime()
                : LocalDateTime.now();
        LocalDate workDate = eventTime.toLocalDate();

        // 1. Lấy danh sách raw logs của nhân viên trong ngày workDate
        List<AttendanceRawLog> todayLogs = rawLogRepository.findByEmployeeIdAndEventTimeBetween(
                employee.getId(),
                workDate.atStartOfDay(),
                workDate.plusDays(1).atStartOfDay()
        );
        todayLogs.sort(Comparator.comparing(AttendanceRawLog::getEventTime));

        // 2. Kiểm tra Cooldown chống spam từ luồng video camera (nếu không phải test giả lập bypass)
        boolean bypassCooldown = Boolean.TRUE.equals(request.getBypassCooldown()) || request.getSimulatedTime() != null;
        if (!bypassCooldown && !todayLogs.isEmpty()) {
            AttendanceRawLog latestLog = todayLogs.get(todayLogs.size() - 1);
            long secondsSinceLast = Math.abs(Duration.between(latestLog.getEventTime(), eventTime).getSeconds());
            if (secondsSinceLast < COOLDOWN_SECONDS) {
                String lastTimeStr = latestLog.getEventTime().format(TIME_FMT);
                return ResponseEntity.ok(FaceScanResponse.builder()
                        .status("COOLDOWN")
                        .type(latestLog.getEventType().name())
                        .time(lastTimeStr)
                        .workDate(workDate.format(DATE_FMT))
                        .employeeId(employee.getId())
                        .employeeCode(employee.getEmployeeCode())
                        .employeeName(employee.getFullName())
                        .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : "")
                        .positionName(employee.getPosition() != null ? employee.getPosition().getName() : "")
                        .deviceId(latestLog.getDeviceId())
                        .eventTime(latestLog.getEventTime())
                        .message(String.format("Đã xác nhận %s lúc %s (Chờ %ds)",
                                latestLog.getEventType().name(), lastTimeStr, COOLDOWN_SECONDS - secondsSinceLast))
                        .build());
            }
        }

        // 3. Tự động xác định EventType (IN hoặc OUT)
        AttendanceEventType eventType;
        if (request.getForceType() != null && !request.getForceType().isBlank()) {
            eventType = "OUT".equalsIgnoreCase(request.getForceType().trim())
                    ? AttendanceEventType.OUT
                    : AttendanceEventType.IN;
        } else if (todayLogs.isEmpty()) {
            eventType = AttendanceEventType.IN;
        } else {
            AttendanceRawLog latestLog = todayLogs.get(todayLogs.size() - 1);
            eventType = (latestLog.getEventType() == AttendanceEventType.IN)
                    ? AttendanceEventType.OUT
                    : AttendanceEventType.IN;
        }

        String deviceId = (request.getDeviceId() != null && !request.getDeviceId().isBlank())
                ? request.getDeviceId().trim()
                : "CAM-FACE-AI-01";
        Double confidence = (request.getConfidence() != null) ? request.getConfidence() : 98.8;

        // 4. Lưu vào AttendanceRawLog và tự động chạy Aggregation
        AttendanceRawLogRequest rawReq = new AttendanceRawLogRequest();
        rawReq.setEmployeeId(employee.getId());
        rawReq.setEventTime(eventTime);
        rawReq.setEventType(eventType);
        rawReq.setSource(AttendanceSource.FACE);
        rawReq.setDeviceId(deviceId);
        rawLogService.recordEvent(rawReq);

        // 5. Lấy AttendanceRecord vừa được tổng hợp và đồng bộ sang bảng Attendance (Legacy)
        AttendanceRecord aggregatedRecord = recordRepository
                .findByEmployeeIdAndWorkDate(employee.getId(), workDate)
                .orElse(null);

        syncLegacyAttendance(employee, workDate, eventTime, eventType, deviceId, aggregatedRecord);

        String timeStr = eventTime.format(TIME_FMT);
        String actionLabel = (eventType == AttendanceEventType.IN) ? "CHECK-IN (Vào ca)" : "CHECK-OUT (Tan ca)";

        log.info("[FACE-ID] {} - {} ({}) lúc {} trên thiết bị {}",
                actionLabel, employee.getFullName(), employee.getEmployeeCode(), timeStr, deviceId);

        return ResponseEntity.ok(FaceScanResponse.builder()
                .status("SUCCESS")
                .type(eventType.name())
                .time(timeStr)
                .workDate(workDate.format(DATE_FMT))
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getFullName())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : "")
                .positionName(employee.getPosition() != null ? employee.getPosition().getName() : "")
                .deviceId(deviceId)
                .confidence(confidence)
                .attendanceStatus(aggregatedRecord != null && aggregatedRecord.getStatus() != null
                        ? aggregatedRecord.getStatus().name()
                        : AttendanceStatus.PRESENT.name())
                .lateMinutes(aggregatedRecord != null ? aggregatedRecord.getLateMinutes() : 0)
                .earlyLeaveMinutes(aggregatedRecord != null ? aggregatedRecord.getEarlyLeaveMinutes() : 0)
                .workedMinutes(aggregatedRecord != null ? aggregatedRecord.getWorkedMinutes() : 0)
                .eventTime(eventTime)
                .message(String.format("Điểm danh khuôn mặt thành công: %s - %s lúc %s",
                        employee.getFullName(), actionLabel, timeStr))
                .build());
    }

    /**
     * Lấy danh sách 30 lượt quét khuôn mặt gần nhất để hiển thị trên màn hình giám sát Camera AI.
     */
    @GetMapping("/recent-face-logs")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<FaceScanResponse>>> getRecentFaceLogs() {
        List<FaceScanResponse> logs = rawLogRepository.findRecentBySource(AttendanceSource.FACE).stream()
                .limit(30)
                .map(r -> {
                    Employee emp = r.getEmployee();
                    return FaceScanResponse.builder()
                            .status("SUCCESS")
                            .type(r.getEventType().name())
                            .time(r.getEventTime().format(TIME_FMT))
                            .workDate(r.getEventTime().toLocalDate().format(DATE_FMT))
                            .employeeId(emp.getId())
                            .employeeCode(emp.getEmployeeCode())
                            .employeeName(emp.getFullName())
                            .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : "")
                            .positionName(emp.getPosition() != null ? emp.getPosition().getName() : "")
                            .deviceId(r.getDeviceId())
                            .confidence(98.9)
                            .eventTime(r.getEventTime())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(logs, "Lấy nhật ký nhận diện khuôn mặt thành công"));
    }

    private void syncLegacyAttendance(
            Employee employee, LocalDate workDate, LocalDateTime eventTime,
            AttendanceEventType eventType, String deviceId, AttendanceRecord aggregatedRecord) {

        Attendance legacy = legacyAttendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), workDate)
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .workDate(workDate)
                        .build());

        if (eventType == AttendanceEventType.IN) {
            if (legacy.getCheckIn() == null || eventTime.isBefore(legacy.getCheckIn())) {
                legacy.setCheckIn(eventTime);
            }
        } else {
            legacy.setCheckOut(eventTime);
        }

        legacy.setDeviceId(deviceId);
        if (aggregatedRecord != null) {
            if (aggregatedRecord.getFirstCheckIn() != null) {
                legacy.setCheckIn(aggregatedRecord.getFirstCheckIn());
            }
            if (aggregatedRecord.getLastCheckOut() != null) {
                legacy.setCheckOut(aggregatedRecord.getLastCheckOut());
            }
            legacy.setLateMinutes(aggregatedRecord.getLateMinutes());
            legacy.setEarlyLeaveMinutes(aggregatedRecord.getEarlyLeaveMinutes());
            legacy.setStatus(aggregatedRecord.getStatus() != null ? aggregatedRecord.getStatus() : AttendanceStatus.PRESENT);
        } else {
            legacy.setStatus(AttendanceStatus.PRESENT);
        }

        legacyAttendanceRepository.save(legacy);
    }
}
