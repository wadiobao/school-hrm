package com.kltn.school_hrm.module.approval.service;

import com.kltn.school_hrm.module.approval.entity.ApprovalLevel;
import com.kltn.school_hrm.module.employee.entity.Employee;

public interface ApproverResolverService {

    /**
     * Xác định người duyệt cụ thể (Employee) dựa trên cấu hình cấp duyệt và nhân viên nộp yêu cầu.
     */
    Employee resolveApprover(ApprovalLevel level, Employee requester);
}
