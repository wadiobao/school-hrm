package com.kltn.school_hrm.configuration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.entity.core.Role;
import com.kltn.school_hrm.enums.Enums.RoleCode;
import com.kltn.school_hrm.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Duyệt qua tất cả giá trị Enum trong Java
        for (RoleCode code : RoleCode.values()) {
            roleRepository.findByRoleCode(code).orElseGet(() -> {
                // Nếu DB chưa có Role này thì tự động Insert vào DB
                Role newRole = Role.builder()
                        .roleCode(code)
                        .roleName(code.name())
                        .description("Tự động khởi tạo từ Enum " + code.name())
                        .build();
                return roleRepository.save(newRole);
            });
        }
    }
}
