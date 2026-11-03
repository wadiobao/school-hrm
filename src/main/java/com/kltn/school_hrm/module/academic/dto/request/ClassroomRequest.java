package com.kltn.school_hrm.module.academic.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomRequest {

    @NotBlank(message = "Mã phòng học không được để trống")
    @Size(max = 30, message = "Mã phòng học không được quá 30 ký tự")
    private String code;

    @NotBlank(message = "Tên phòng học không được để trống")
    @Size(max = 100, message = "Tên phòng học không được quá 100 ký tự")
    private String name;

    @Positive(message = "Sức chứa phòng học phải lớn hơn 0")
    private Integer capacity;

    private ClassroomType roomType;

    @NotNull(message = "Trạng thái phòng học không được để trống")
    private ClassroomStatus status;
}

