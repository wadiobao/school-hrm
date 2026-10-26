package com.kltn.school_hrm.module.employee.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.employee.dto.request.ContractRequest;
import com.kltn.school_hrm.module.employee.dto.request.SalaryChangeRequest;
import com.kltn.school_hrm.module.employee.dto.response.ContractResponse;
import com.kltn.school_hrm.module.employee.dto.response.SalaryHistoryResponse;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.employee.service.ContractService;
import com.kltn.school_hrm.module.employee.service.SalaryHistoryService;
import com.kltn.school_hrm.shared.configuration.dto.CustomPrincipal;
import com.kltn.school_hrm.shared.exception.custom.NotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final SalaryHistoryService salaryHistoryService;
    private final EmployeeRepository employeeRepository;

    /** Tạo hợp đồng mới (Chỉ HR_MANAGER, SUPER_ADMIN) */
    @PostMapping
    @PreAuthorize("@contractSecurity.canManageContracts()")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contractService.createContract(request), "Tạo hợp đồng mới thành công"));
    }

    /** Gia hạn / Ký mới hợp đồng kế tiếp (Chỉ HR_MANAGER, SUPER_ADMIN) */
    @PostMapping("/{id}/renew")
    @PreAuthorize("@contractSecurity.canManageContracts()")
    public ResponseEntity<ApiResponse<ContractResponse>> renewContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contractService.renew(id, request), "Gia hạn / Ký mới hợp đồng thành công"));
    }

    /** Xem chi tiết 1 hợp đồng */
    @GetMapping("/{id}")
    @PreAuthorize("@contractSecurity.canViewContract(#id)")
    public ResponseEntity<ApiResponse<ContractResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContractById(id), "Lấy thông tin hợp đồng thành công"));
    }

    /** Xem toàn bộ lịch sử hợp đồng của nhân viên (Nhân viên chỉ xem được của mình, Quản lý xem toàn trường) */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@contractSecurity.canViewEmployeeContracts(#employeeId)")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractHistory(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getContractHistory(employeeId), "Lấy lịch sử hợp đồng thành công"));
    }

    /** Xem hợp đồng đang có hiệu lực hiện tại của nhân viên */
    @GetMapping("/employee/{employeeId}/current")
    @PreAuthorize("@contractSecurity.canViewEmployeeContracts(#employeeId)")
    public ResponseEntity<ApiResponse<ContractResponse>> getCurrentContract(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getCurrentContract(employeeId), "Lấy hợp đồng hiện tại thành công"));
    }

    /** Endpoint tiện ích: Nhân viên tự xem danh sách hợp đồng của chính mình */
    @GetMapping("/my-contracts")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getMyContracts(
            @AuthenticationPrincipal CustomPrincipal principal) {
        Employee currentEmployee = employeeRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nhân viên liên kết với tài khoản này"));
        return ResponseEntity.ok(ApiResponse.success(
                contractService.getContractHistory(currentEmployee.getId()),
                "Lấy lịch sử hợp đồng của bạn thành công"));
    }

    /** Endpoint tiện ích: Nhân viên tự xem hợp đồng hiện tại của chính mình */
    @GetMapping("/my-current")
    public ResponseEntity<ApiResponse<ContractResponse>> getMyCurrentContract(
            @AuthenticationPrincipal CustomPrincipal principal) {
        Employee currentEmployee = employeeRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nhân viên liên kết với tài khoản này"));
        return ResponseEntity.ok(ApiResponse.success(
                contractService.getCurrentContract(currentEmployee.getId()),
                "Lấy hợp đồng hiện tại của bạn thành công"));
    }

    /** Cập nhật thông tin hợp đồng (Có kiểm soát bất biến các trường cốt lõi) */
    @PutMapping("/{id}")
    @PreAuthorize("@contractSecurity.canManageContracts()")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.updateContract(id, request), "Cập nhật hợp đồng thành công"));
    }

    /** Chấm dứt hợp đồng (Terminate) */
    @PutMapping("/{id}/terminate")
    @PreAuthorize("@contractSecurity.canManageContracts()")
    public ResponseEntity<ApiResponse<Void>> terminateContract(@PathVariable Long id) {
        contractService.terminate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Chấm dứt hợp đồng thành công"));
    }

    /** Xóa hợp đồng nháp / chưa phát sinh nghiệp vụ */
    @DeleteMapping("/{id}")
    @PreAuthorize("@contractSecurity.canManageContracts()")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa hợp đồng thành công"));
    }

    /**
     * Điều chỉnh mức lương của hợp đồng lao động và lưu vết SalaryHistory.
     * Chỉ SUPER_ADMIN và HR_MANAGER có thẩm quyền phê duyệt tăng/giảm lương.
     */
    @PostMapping("/{id}/change-salary")
    @PreAuthorize("@contractSecurity.canManageContracts()")
    public ResponseEntity<ApiResponse<SalaryHistoryResponse>> changeSalary(
            @PathVariable Long id,
            @Valid @RequestBody SalaryChangeRequest request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        Long approverId = (principal != null) ? principal.userId() : null;
        SalaryHistoryResponse response = salaryHistoryService.changeSalary(id, request, approverId);
        return ResponseEntity.ok(ApiResponse.success(response, "Điều chỉnh mức lương hợp đồng thành công"));
    }

    /**
     * Xem toàn bộ lịch sử diễn biến lương của một hợp đồng lao động cụ thể.
     * Nhân viên được phép xem lịch sử lương của hợp đồng thuộc sở hữu của mình.
     */
    @GetMapping("/{id}/salary-history")
    @PreAuthorize("@contractSecurity.canViewContract(#id)")
    public ResponseEntity<ApiResponse<List<SalaryHistoryResponse>>> getSalaryHistory(
            @PathVariable Long id) {
        List<SalaryHistoryResponse> history = salaryHistoryService.getHistoryByContract(id);
        return ResponseEntity.ok(ApiResponse.success(history, "Lấy lịch sử điều chỉnh lương thành công"));
    }
}

