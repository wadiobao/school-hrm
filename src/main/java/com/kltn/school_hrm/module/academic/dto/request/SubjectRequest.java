package com.kltn.school_hrm.module.academic.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectRequest {

    @NotBlank(message = "Mã môn học không được để trống")
    @Size(max = 30, message = "Mã môn học không được quá 30 ký tự")
    private String code;

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 100, message = "Tên môn học không được quá 100 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Trạng thái môn học không được để trống")
    private SubjectStatus status;
}

