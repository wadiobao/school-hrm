package com.kltn.school_hrm.entity.employee;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.DegreeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class DegreeAndCertificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private DegreeType type;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 200)
	private String institution;

	@Column(name = "issue_date")
	private LocalDate issueDate;

	@Column(name = "attachment_url", length = 500)
	private String attachmentUrl;
}
