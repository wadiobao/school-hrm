package com.kltn.school_hrm.dto.response;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.WorkPermitStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkPermitResponse {
	private Long id;
	private String workPermitNumber;
	private LocalDate wpIssueDate;
	private LocalDate wpExpiryDate;
	private WorkPermitStatus wpStatus;
	private String trcNumber;
	private LocalDate trcExpiryDate;
	private String documentScanUrl;
}
