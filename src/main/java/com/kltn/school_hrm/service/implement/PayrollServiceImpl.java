package com.kltn.school_hrm.service.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.dto.response.PayrollResponse;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.entity.payroll.Payroll;
import com.kltn.school_hrm.entity.payroll.PayrollDetail;
import com.kltn.school_hrm.entity.payroll.SalaryComponent;
import com.kltn.school_hrm.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.PayrollDetailRepository;
import com.kltn.school_hrm.repository.PayrollRepository;
import com.kltn.school_hrm.repository.SalaryComponentRepository;
import com.kltn.school_hrm.service.PayrollService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollDetailRepository payrollDetailRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryComponentRepository salaryComponentRepository;

    @Override
    @Transactional
    public PayrollResponse createPayroll(PayrollCreateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Đảm bảo không tạo bảng lương trùng tháng/năm cho cùng nhân viên
        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                request.getEmployeeId(), request.getMonth(), request.getYear())) {
            throw new RuntimeException("Payroll already exists for this employee in " 
                + request.getMonth() + "/" + request.getYear());
        }

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(request.getMonth())
                .year(request.getYear())
                .grossSalary(request.getGrossSalary())
                .totalWorkDays(request.getTotalWorkDays())
                .totalTeachingHours(request.getTotalTeachingHours())
                .totalAllowances(request.getTotalAllowances())
                .totalDeductions(request.getTotalDeductions())
                .netSalary(request.getNetSalary())
                .status(request.getStatus() != null ? request.getStatus() : PayrollStatus.DRAFT)
                .build();

        payroll = payrollRepository.save(payroll);

        // Lưu chi tiết các khoản lương
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<PayrollDetail> details = new ArrayList<>();
            for (PayrollCreateRequest.PayrollDetailItem item : request.getDetails()) {
                SalaryComponent component = salaryComponentRepository.findById(item.getComponentId())
                        .orElseThrow(() -> new RuntimeException("Salary component not found: " + item.getComponentId()));
                details.add(PayrollDetail.builder()
                        .payroll(payroll)
                        .component(component)
                        .amount(item.getAmount())
                        .build());
            }
            payrollDetailRepository.saveAll(details);
            payroll.setDetails(details);
        }

        return mapToResponse(payroll);
    }

    @Override
    @Transactional
    public PayrollResponse updatePayroll(Long id, PayrollCreateRequest request) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        if (payroll.getStatus() == PayrollStatus.PAID) {
            throw new RuntimeException("Cannot update a payroll that has already been paid");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        payroll.setEmployee(employee);
        payroll.setMonth(request.getMonth());
        payroll.setYear(request.getYear());
        payroll.setGrossSalary(request.getGrossSalary());
        payroll.setTotalWorkDays(request.getTotalWorkDays());
        payroll.setTotalTeachingHours(request.getTotalTeachingHours());
        payroll.setTotalAllowances(request.getTotalAllowances());
        payroll.setTotalDeductions(request.getTotalDeductions());
        payroll.setNetSalary(request.getNetSalary());

        if (request.getStatus() != null) {
            payroll.setStatus(request.getStatus());
        }

        // Cập nhật chi tiết: xóa cũ, thêm mới
        if (request.getDetails() != null) {
            payrollDetailRepository.deleteAll(payrollDetailRepository.findByPayrollId(id));
            List<PayrollDetail> newDetails = new ArrayList<>();
            for (PayrollCreateRequest.PayrollDetailItem item : request.getDetails()) {
                SalaryComponent component = salaryComponentRepository.findById(item.getComponentId())
                        .orElseThrow(() -> new RuntimeException("Salary component not found: " + item.getComponentId()));
                newDetails.add(PayrollDetail.builder()
                        .payroll(payroll)
                        .component(component)
                        .amount(item.getAmount())
                        .build());
            }
            payrollDetailRepository.saveAll(newDetails);
            payroll.setDetails(newDetails);
        }

        payroll = payrollRepository.save(payroll);
        return mapToResponse(payroll);
    }

    @Override
    public PayrollResponse getPayrollById(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        return mapToResponse(payroll);
    }

    @Override
    public List<PayrollResponse> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PayrollResponse> getPayrollsByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PayrollResponse> getPayrollsByMonthAndYear(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PayrollResponse updatePayrollStatus(Long id, PayrollStatus status) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setStatus(status);
        payroll = payrollRepository.save(payroll);
        return mapToResponse(payroll);
    }

    @Override
    @Transactional
    public void deletePayroll(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        if (payroll.getStatus() == PayrollStatus.PAID) {
            throw new RuntimeException("Cannot delete a payroll that has already been paid");
        }
        payrollRepository.deleteById(id);
    }

    private PayrollResponse mapToResponse(Payroll payroll) {
        List<PayrollResponse.PayrollDetailResponse> detailResponses = new ArrayList<>();
        if (payroll.getDetails() != null) {
            detailResponses = payroll.getDetails().stream()
                    .map(d -> PayrollResponse.PayrollDetailResponse.builder()
                            .id(d.getId())
                            .componentId(d.getComponent().getId())
                            .componentCode(d.getComponent().getCode())
                            .componentName(d.getComponent().getName())
                            .componentType(d.getComponent().getType())
                            .amount(d.getAmount())
                            .build())
                    .collect(Collectors.toList());
        }

        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployee().getId())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .grossSalary(payroll.getGrossSalary())
                .totalWorkDays(payroll.getTotalWorkDays())
                .totalTeachingHours(payroll.getTotalTeachingHours())
                .totalAllowances(payroll.getTotalAllowances())
                .totalDeductions(payroll.getTotalDeductions())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus())
                .createdAt(payroll.getCreatedAt())
                .details(detailResponses)
                .build();
    }
}
