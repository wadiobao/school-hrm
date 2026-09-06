package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.dto.response.LeaveResponse;
import com.kltn.school_hrm.entity.attendance.LeaveBalance;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.LeaveBalanceRepository;
import com.kltn.school_hrm.repository.LeaveRequestRepository;
import com.kltn.school_hrm.repository.UserRepository;
import com.kltn.school_hrm.service.LeaveService;
import com.kltn.school_hrm.utils.LeaveDayCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveDayCalculator leaveDayCalculator;

    // Các trạng thái không cho phép tạo đơn nghỉ trùng lặp
    private static final List<RequestStatus> BLOCKING_STATUSES = List.of(
            RequestStatus.PENDING,
            RequestStatus.APPROVED);

    @Override
    @Transactional
    public LeaveResponse createLeaveRequest(LeaveCreateRequest request) {
        // Kiểm tra employee có tồn tại không
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // Kiểm tra có đang working không
        if (employee.getStatus() != EmployeeStatus.WORKING) {
            throw new RuntimeException("Nhân viên không đang làm việc");
        }

        // Kiểm tra ngày nghỉ có hợp lệ không
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        // Kiểm tra có đơn nghỉ trùng lặp không
        if (leaveRequestRepository.existsOverlap(
                request.getEmployeeId(),
                BLOCKING_STATUSES,
                request.getStartDate(),
                request.getEndDate())) {
            throw new RuntimeException("Bạn đã có đơn nghỉ trùng lặp");
        }

        // Tính số ngày nghỉ
        int leaveDays = leaveDayCalculator.calculate(request.getStartDate(), request.getEndDate());

        if (leaveDays <= 0) {
            throw new BusinessException("Cần phải nghỉ ít nhất 1 ngày làm việc"); 
        }

        // Kiểm tra số ngày nghỉ
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeIdAndYear(employee.getId(), request.getStartDate().getYear())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin số ngày nghỉ"));

        // Kiểm tra có đủ ngày nghỉ không
        if (leaveBalance.getRemainingDays().compareTo(BigDecimal.valueOf(leaveDays)) < 0) {
            throw new BusinessException("Bạn không có đủ ngày nghỉ");
        }

        // Tạo đơn nghỉ
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        if (request.getSubstituteTeacherId() != null) {
            Employee substitute = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new RuntimeException("Substitute teacher not found"));
            leaveRequest.setSubstituteTeacher(substitute);
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveResponse updateLeaveRequest(Long id, LeaveCreateRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leaveRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Can only update pending leave requests");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());

        if (request.getSubstituteTeacherId() != null) {
            Employee substitute = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new RuntimeException("Substitute teacher not found"));
            leaveRequest.setSubstituteTeacher(substitute);
        } else {
            leaveRequest.setSubstituteTeacher(null);
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    public LeaveResponse getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        return mapToResponse(leaveRequest);
    }

    @Override
    public List<LeaveResponse> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getLeaveRequestsByEmployeeId(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveResponse approveLeaveRequest(Long id, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver user not found"));

        leaveRequest.setStatus(RequestStatus.APPROVED);
        leaveRequest.setApprover(approver);

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeaveRequest(Long id, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver user not found"));

        leaveRequest.setStatus(RequestStatus.REJECTED);
        leaveRequest.setApprover(approver);

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional
    public void deleteLeaveRequest(Long id) {
        if (!leaveRequestRepository.existsById(id)) {
            throw new RuntimeException("Leave request not found");
        }
        leaveRequestRepository.deleteById(id);
    }

    private LeaveResponse mapToResponse(LeaveRequest leaveRequest) {
        return LeaveResponse.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .substituteTeacherId(
                        leaveRequest.getSubstituteTeacher() != null ? leaveRequest.getSubstituteTeacher().getId()
                                : null)
                .approverId(leaveRequest.getApprover() != null ? leaveRequest.getApprover().getId() : null)
                .status(leaveRequest.getStatus())
                .build();
    }
}
