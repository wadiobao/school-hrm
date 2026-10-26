package com.kltn.school_hrm.module.employee.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.module.employee.dto.request.SalaryChangeRequest;
import com.kltn.school_hrm.module.employee.dto.response.SalaryHistoryResponse;
import com.kltn.school_hrm.module.employee.entity.SalaryHistory;
import com.kltn.school_hrm.shared.enums.Enums.Currency;
import com.kltn.school_hrm.shared.enums.Enums.SalaryChangeReason;

/**
 * Service quản lý lịch sử điều chỉnh lương và thực hiện thay đổi lương của hợp đồng.
 */
public interface SalaryHistoryService {

    /**
     * Điều chỉnh mức lương hợp đồng và lưu vết SalaryHistory theo DTO.
     *
     * @param contractId       ID của hợp đồng cần thay đổi lương
     * @param request          Dữ liệu điều chỉnh lương
     * @param approvedByUserId ID người dùng thực hiện phê duyệt
     * @return SalaryHistoryResponse thông tin điều chỉnh đã lưu
     */
    SalaryHistoryResponse changeSalary(Long contractId, SalaryChangeRequest request, Long approvedByUserId);

    /**
     * Phương thức điều chỉnh lương nhận tham số chi tiết.
     */
    SalaryHistory changeSalary(
            Long contractId,
            BigDecimal newSalary,
            Currency currency,
            LocalDate effectiveDate,
            String decisionNumber,
            SalaryChangeReason reason,
            String note,
            Long approvedBy);

    /**
     * Lấy toàn bộ lịch sử điều chỉnh lương của hợp đồng theo thứ tự mới nhất trước.
     */
    List<SalaryHistoryResponse> getHistoryByContract(Long contractId);

    /**
     * Lấy bản ghi điều chỉnh lương mới nhất của hợp đồng.
     */
    SalaryHistoryResponse getLatest(Long contractId);
}
