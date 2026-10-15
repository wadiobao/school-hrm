package com.kltn.school_hrm.module.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
	private Long userId;
	private String username;
	private String email;
	private String fullName;
	private String roleCode;
	private String roleName;
	private Long employeeId;
	private String employeeCode;
	private String accessToken;
}
