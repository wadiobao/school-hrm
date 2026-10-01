package com.kltn.school_hrm.module.core.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.RoleCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreateRequest {
    @NotNull(message = "Role Code is required")
    private RoleCode roleCode;

    @NotBlank(message = "Role Name is required")
    private String roleName;

    private String description;
}
