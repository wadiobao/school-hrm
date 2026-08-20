package com.kltn.school_hrm.dto.response;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.enums.Enums.LeaveType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Long substituteTeacherId;
    private Long approverId;
    private RequestStatus status;
}
