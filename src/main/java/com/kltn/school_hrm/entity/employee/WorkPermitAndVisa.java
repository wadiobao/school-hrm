package com.kltn.school_hrm.entity.employee;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.WorkPermitStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPermitAndVisa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false, unique = true)
	private Employee employee;

	@Column(name = "work_permit_number", length = 50)
	private String workPermitNumber;

	@Column(name = "wp_issue_date")
	private LocalDate wpIssueDate;

	@Column(name = "wp_expiry_date")
	private LocalDate wpExpiryDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "wp_status", length = 20)
	private WorkPermitStatus wpStatus;

	@Column(name = "trc_number", length = 50)
	private String trcNumber; // Temporary Residence Card (Thẻ tạm trú)

	@Column(name = "trc_expiry_date")
	private LocalDate trcExpiryDate;

	@Column(name = "document_scan_url", length = 500)
	private String documentScanUrl;
}