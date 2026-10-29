package com.kltn.school_hrm.module.core.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateManagerRequest {
	private Long id;
	private Long employeeId; // null = Bãi nhiệm (UNASSIGN)
}
