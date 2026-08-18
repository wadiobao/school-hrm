package com.kltn.school_hrm.dto.request;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.WorkPermitStatus;

import lombok.Data;

@Data
public class WorkPermitRequest {
	private String workPermitNumber;
	private LocalDate wpIssueDate;
	private LocalDate wpExpiryDate;
	private WorkPermitStatus wpStatus;
	private String trcNumber;
	private LocalDate trcExpiryDate;
	private String documentScanUrl;
}
