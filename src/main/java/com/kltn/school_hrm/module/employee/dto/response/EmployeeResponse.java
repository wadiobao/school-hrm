package com.kltn.school_hrm.module.employee.dto.response;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {
	private Long id;
	private String employeeCode;
	private String fullName;
	private String nativeName;
	private LocalDate dateOfBirth;
	private String gender;
	private String email;
	private String phone;
	private String address;
	private String citizenId;
	private Long departmentId;
	private String departmentName;
	private Long positionId;
	private String positionName;
	private EmployeeStatus status;
	private LocalDate joinDate;
	private WorkPermitResponse workPermitAndVisa;
	private Long userId;
	private String username;
	private String roleCode;
	private Boolean hasFaceSample;
	private String faceImageUrl;
}
