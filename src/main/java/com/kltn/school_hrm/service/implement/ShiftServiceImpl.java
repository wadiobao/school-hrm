package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.ShiftRequest;
import com.kltn.school_hrm.dto.response.ShiftResponse;
import com.kltn.school_hrm.entity.attendance.Shift;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.EmployeeShiftAssignmentRepository;
import com.kltn.school_hrm.repository.ShiftRepository;
import com.kltn.school_hrm.service.ShiftService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;

    @Override
    public ShiftResponse createShift(ShiftRequest request) {
        // Validate thời gian làm việc
        validateShiftTimes(request);

        // Mã ca phải duy nhất
        if (shiftRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã ca làm việc đã tồn tại: " + request.getCode());
        }

        Shift shift = Shift.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .overNight(request.getOverNight() != null ? request.getOverNight() : false)
                .breakMinutes(request.getBreakMinutes() != null ? request.getBreakMinutes() : 0)
                .graceMinutes(request.getGraceMinutes() != null ? request.getGraceMinutes() : 0)
                .build();

        return mapToResponse(shiftRepository.save(shift));
    }

    @Override
    public ShiftResponse updateShift(Long id, ShiftRequest request) {
        Shift shift = getShiftEntityById(id);

        // Validate thời gian làm việc
        validateShiftTimes(request);

        // Kiểm tra trùng mã với ca khác
        if (shiftRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException("Mã ca làm việc đã tồn tại: " + request.getCode());
        }

        shift.setName(request.getName());
        shift.setCode(request.getCode().toUpperCase());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        if (request.getOverNight() != null) shift.setOverNight(request.getOverNight());
        if (request.getBreakMinutes() != null) shift.setBreakMinutes(request.getBreakMinutes());
        if (request.getGraceMinutes() != null) shift.setGraceMinutes(request.getGraceMinutes());
        if (request.getIsActive() != null) shift.setIsActive(request.getIsActive());

        return mapToResponse(shiftRepository.save(shift));
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long id) {
        return mapToResponse(getShiftEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        return shiftRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getActiveShifts() {
        return shiftRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteShift(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy ca làm việc với id: " + id);
        }

        // Rule service.md: Không delete Shift đã được sử dụng
        if (assignmentRepository.existsByShiftId(id)) {
            throw new BusinessException("Không thể xóa ca làm việc này vì đã có phân công cho nhân viên. Hãy dùng chức năng tạm ngưng (deactivate) thay thế.");
        }

        shiftRepository.deleteById(id);
    }

    @Override
    public void activateShift(Long id) {
        Shift shift = getShiftEntityById(id);
        shift.setIsActive(true);
        shiftRepository.save(shift);
    }

    @Override
    public void deactivateShift(Long id) {
        Shift shift = getShiftEntityById(id);
        shift.setIsActive(false);
        shiftRepository.save(shift);
    }

    @Override
    public ShiftResponse toggleActive(Long id) {
        Shift shift = getShiftEntityById(id);
        // Đảo trạng thái active
        shift.setIsActive(!Boolean.TRUE.equals(shift.getIsActive()));
        return mapToResponse(shiftRepository.save(shift));
    }

    private void validateShiftTimes(ShiftRequest request) {
        boolean isOvernight = Boolean.TRUE.equals(request.getOverNight());
        if (!isOvernight && !request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("Giờ bắt đầu phải trước giờ kết thúc đối với ca làm việc không qua đêm.");
        }
        if (isOvernight && !request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("Ca qua đêm phải có giờ bắt đầu sau giờ kết thúc trong ngày (ví dụ: 22:00 -> 06:00).");
        }
    }

    private Shift getShiftEntityById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca làm việc với id: " + id));
    }

    private ShiftResponse mapToResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .name(shift.getName())
                .code(shift.getCode())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .overNight(shift.getOverNight())
                .breakMinutes(shift.getBreakMinutes())
                .graceMinutes(shift.getGraceMinutes())
                .isActive(shift.getIsActive())
                .build();
    }
}
