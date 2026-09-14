package com.kltn.school_hrm.service.approval;

import com.kltn.school_hrm.entity.approval.ApprovalLevel;
import com.kltn.school_hrm.entity.employee.Employee;

public interface ApproverResolverService {

    /**
     * Xác định người duyệt cụ thể (Employee) dựa trên cấu hình cấp duyệt và nhân viên nộp yêu cầu.
     */
    Employee resolveApprover(ApprovalLevel level, Employee requester);
}
