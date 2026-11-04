package com.kltn.school_hrm.module.scheduling.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableRequest {

    @NotNull(message = "ID lớp học không được để trống")
    private Long schoolClassId;

    @NotNull(message = "ID học kỳ không được để trống")
    private Long semesterId;
}

