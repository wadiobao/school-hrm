package com.kltn.school_hrm.service.implement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.AssignShiftRequest;
import com.kltn.school_hrm.dto.request.ChangeShiftRequest;
import com.kltn.school_hrm.dto.request.EndAssignmentRequest;
import com.kltn.school_hrm.dto.response.EmployeeShiftAssignmentResponse;
import com.kltn.school_hrm.entity.attendance.EmployeeShiftAssignment;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.EmployeeShiftAssignmentRepository;
import com.kltn.school_hrm.repository.ShiftRepository;
import com.kltn.school_hrm.service.EmployeeShiftAssignmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EmployeeShiftAssignmentServiceImpl implements EmployeeShiftAssignmentService {

    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    @Override
    public EmployeeShiftAssignmentResponse assign(AssignShiftRequest request) {
        // 1. Employee phải tồn tại
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + request.getEmployeeId()));

        // 2. Shift phải tồn tại
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca làm việc với ID: " + request.getShiftId()));

        // 3. Shift phải active
        if (!Boolean.TRUE.equals(shift.getIsActive())) {
            throw new BusinessException("Ca làm việc hiện đang không hoạt động (inactive), không thể phân công.");
        }

        // 4. effectiveFrom <= effectiveTo
        if (request.getEffectiveTo() != null && request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new BusinessException("Ngày bắt đầu (effectiveFrom) phải trước hoặc bằng ngày kết thúc (effectiveTo).");
        }

        // 5. Kiểm tra trùng lặp (Overlap check)
        boolean hasOverlap = assignmentRepository.existsOverlappingAssignment(
                employee.getId(),
                request.getDayOfWeek(),
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                null
        );
        if (hasOverlap) {
            throw new BusinessException(String.format(
                    "Nhân viên %s đã được phân công ca làm việc vào %s trong khoảng thời gian này và bị trùng lặp.",
                    employee.getFullName(), request.getDayOfWeek()
            ));
        }

        EmployeeShiftAssignment assignment = EmployeeShiftAssignment.builder()
                .employee(employee)
                .shift(shift)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .dayOfWeek(request.getDayOfWeek())
                .active(true)
                .build();

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    public EmployeeShiftAssignmentResponse changeShift(Long assignmentId, ChangeShiftRequest request) {
        EmployeeShiftAssignment oldAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ca với ID: " + assignmentId));

        if (!Boolean.TRUE.equals(oldAssignment.getActive())) {
            throw new BusinessException("Phân công ca này đã bị vô hiệu hóa, không thể đổi ca.");
        }

        Shift newShift = shiftRepository.findById(request.getNewShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca làm việc mới với ID: " + request.getNewShiftId()));

        if (!Boolean.TRUE.equals(newShift.getIsActive())) {
            throw new BusinessException("Ca làm việc mới đang không hoạt động (inactive).");
        }

        LocalDate newEffectiveFrom = request.getEffectiveFrom();
        if (newEffectiveFrom.isBefore(oldAssignment.getEffectiveFrom())) {
            throw new BusinessException("Ngày bắt đầu ca mới phải sau hoặc bằng ngày bắt đầu của phân ca cũ (" + oldAssignment.getEffectiveFrom() + ").");
        }

        // Đóng phân ca cũ: effectiveTo = newEffectiveFrom.minusDays(1)
        LocalDate oldEffectiveTo = newEffectiveFrom.minusDays(1);
        if (oldEffectiveTo.isBefore(oldAssignment.getEffectiveFrom())) {
            // Nếu đổi ngay tại ngày bắt đầu của ca cũ, ta có thể kết thúc hoặc deactivate ca cũ
            oldAssignment.setActive(false);
            oldAssignment.setEffectiveTo(oldAssignment.getEffectiveFrom());
        } else {
            oldAssignment.setEffectiveTo(oldEffectiveTo);
        }
        assignmentRepository.save(oldAssignment);

        // Kiểm tra overlap cho phân ca mới (loại trừ record cũ đã được điều chỉnh)
        boolean hasOverlap = assignmentRepository.existsOverlappingAssignment(
                oldAssignment.getEmployee().getId(),
                oldAssignment.getDayOfWeek(),
                newEffectiveFrom,
                null,
                oldAssignment.getId()
        );
        if (hasOverlap) {
            throw new BusinessException("Phân ca mới bị trùng lặp với một phân ca khác đang có.");
        }

        // Tạo phân ca mới
        EmployeeShiftAssignment newAssignment = EmployeeShiftAssignment.builder()
                .employee(oldAssignment.getEmployee())
                .shift(newShift)
                .effectiveFrom(newEffectiveFrom)
                .effectiveTo(null)
                .dayOfWeek(oldAssignment.getDayOfWeek())
                .active(true)
                .build();

        return mapToResponse(assignmentRepository.save(newAssignment));
    }

    @Override
    public void endAssignment(Long assignmentId, EndAssignmentRequest request) {
        EmployeeShiftAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ca với ID: " + assignmentId));

        if (request.getEffectiveTo().isBefore(assignment.getEffectiveFrom())) {
            throw new BusinessException("Ngày kết thúc không thể trước ngày bắt đầu (" + assignment.getEffectiveFrom() + ").");
        }

        assignment.setEffectiveTo(request.getEffectiveTo());
        // Nếu ngày kết thúc đã qua so với hôm nay, có thể giữ active hoặc để logic tìm kiếm theo ngày quyết định
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeShiftAssignmentResponse> findForDate(Long employeeId, LocalDate date) {
        return assignmentRepository.findApplicableAssignment(employeeId, date.getDayOfWeek(), date)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeShiftAssignmentResponse> getEmployeeSchedule(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + employeeId);
        }
        return assignmentRepository.findActiveScheduleByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeShiftAssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAllWithDetails().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EmployeeShiftAssignmentResponse mapToResponse(EmployeeShiftAssignment esa) {
        return EmployeeShiftAssignmentResponse.builder()
                .id(esa.getId())
                .employeeId(esa.getEmployee().getId())
                .employeeCode(esa.getEmployee().getEmployeeCode())
                .employeeName(esa.getEmployee().getFullName())
                .shiftId(esa.getShift().getId())
                .shiftCode(esa.getShift().getCode())
                .shiftName(esa.getShift().getName())
                .startTime(esa.getShift().getStartTime())
                .endTime(esa.getShift().getEndTime())
                .overnight(esa.getShift().getOverNight())
                .effectiveFrom(esa.getEffectiveFrom())
                .effectiveTo(esa.getEffectiveTo())
                .dayOfWeek(esa.getDayOfWeek())
                .active(esa.getActive())
                .build();
    }
}
