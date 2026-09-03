package com.kltn.school_hrm.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.enums.Enums.ContractType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRequest {

    @NotBlank(message = "Số hợp đồng không được để trống")
    @Size(max = 50, message = "Số hợp đồng không vượt quá 50 ký tự")
    private String contractNumber;

    @NotNull(message = "ID nhân viên không được để trống")
    private Long employeeId;

    @NotNull(message = "ID vị trí không được để trống")
    private Long positionId;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    private LocalDate endDate; // Để null nếu loại hợp đồng là INDEFINITE_TERM

    @NotNull(message = "Loại hợp đồng không được để trống")
    private ContractType type;

    private ContractStatus status; // Có thể null khi Tạo mới (Service tự set ACTIVE), truyền giá trị khi Update

    @NotNull(message = "Lương gross không được để trống")
    @DecimalMin(value = "0.0", message = "Lương gross phải lớn hơn hoặc bằng 0")
    private BigDecimal grossSalary;

    @NotBlank(message = "Đơn vị tiền tệ không được để trống")
    @Pattern(regexp = "^(VND|USD)$", message = "Đơn vị tiền tệ chỉ chấp nhận VND hoặc USD")
    private String currency;

    @DecimalMin(value = "0.0", message = "Phụ cấp nhà ở phải lớn hơn hoặc bằng 0")
    private BigDecimal housingAllowance;

    @DecimalMin(value = "0.0", message = "Phụ cấp vé máy bay phải lớn hơn hoặc bằng 0")
    private BigDecimal flightAllowance;

    @DecimalMin(value = "0.0", message = "Phụ cấp di dời phải lớn hơn hoặc bằng 0")
    private BigDecimal relocationAllowance;
}
