package com.kltn.school_hrm.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCreateRequest {

	@NotBlank(message = "Mã nhân viên không được để trống")
	private String employeeCode;

	@NotBlank(message = "Họ và tên không được để trống")
	private String fullName;

	private String nativeName;

	private LocalDate dateOfBirth;

	private String gender;

	@NotBlank(message = "Email không được để trống")
	@Email(message = "Email không đúng định dạng")
	private String email;

	private String phone;

	private String address;

	@NotNull(message = "Phòng ban/Khoa là bắt buộc")
	private Long departmentId;

	@NotNull(message = "Chức vụ là bắt buộc")
	private Long positionId;

	private LocalDate joinDate;

	// ID tài khoản User tương ứng (nếu đã tạo tài khoản đăng nhập trước đó)
	private Long userId;
}
