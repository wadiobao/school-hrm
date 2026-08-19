package com.kltn.school_hrm.dto.response;

import com.kltn.school_hrm.enums.Enums.RoleCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Long id;
    private RoleCode roleCode;
    private String roleName;
    private String description;
}
