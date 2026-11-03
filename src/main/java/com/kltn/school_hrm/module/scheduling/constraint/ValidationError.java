package com.kltn.school_hrm.module.scheduling.constraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {
    private String ruleId; // e.g., BR-14, BR-15, BR-16, BR-18
    private String message;
    private String severity; // ERROR, WARNING
    private Long entryId;
    private String details;
}

