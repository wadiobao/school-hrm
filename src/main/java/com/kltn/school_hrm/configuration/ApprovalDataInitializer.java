package com.kltn.school_hrm.configuration;

import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kltn.school_hrm.entity.approval.ApprovalLevel;
import com.kltn.school_hrm.entity.approval.ApprovalPolicy;
import com.kltn.school_hrm.enums.Enums.ApprovalConditionType;
import com.kltn.school_hrm.enums.Enums.ApproverType;
import com.kltn.school_hrm.repository.ApprovalPolicyRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ApprovalDataInitializer {

    @Bean
    public CommandLineRunner initDefaultApprovalPolicies(ApprovalPolicyRepository approvalPolicyRepository) {
        return args -> {
            String leavePolicyCode = "LEAVE_REQUEST_POLICY";
            if (approvalPolicyRepository.findByCodeAndVersion(leavePolicyCode, 1).isEmpty()) {
                log.info("Khởi tạo Approval Policy mặc định cho đơn nghỉ phép: {}", leavePolicyCode);

                ApprovalPolicy policy = ApprovalPolicy.builder()
                        .code(leavePolicyCode)
                        .name("Chính sách phê duyệt nghỉ phép trường học")
                        .businessType("LEAVE_REQUEST")
                        .version(1)
                        .isActive(true)
                        .description("Level 1: Quản lý trực tiếp (bắt buộc). Level 2: Quản lý cấp trên / Ban Giám hiệu nếu nghỉ từ 2 ngày trở lên.")
                        .levels(new ArrayList<>())
                        .build();

                ApprovalLevel level1 = ApprovalLevel.builder()
                        .policy(policy)
                        .levelOrder(1)
                        .levelName("Quản lý trực tiếp / Trưởng bộ môn")
                        .approverType(ApproverType.DIRECT_MANAGER)
                        .conditionType(ApprovalConditionType.NONE)
                        .build();

                ApprovalLevel level2 = ApprovalLevel.builder()
                        .policy(policy)
                        .levelOrder(2)
                        .levelName("Quản lý cấp trên / Ban Giám hiệu")
                        .approverType(ApproverType.PARENT_DEPARTMENT_MANAGER)
                        .conditionType(ApprovalConditionType.MIN_LEAVE_DAYS)
                        .conditionValue("2")
                        .build();

                policy.getLevels().add(level1);
                policy.getLevels().add(level2);

                approvalPolicyRepository.save(policy);
                log.info("Khởi tạo thành công Approval Policy cho LEAVE_REQUEST");
            }
        };
    }
}
