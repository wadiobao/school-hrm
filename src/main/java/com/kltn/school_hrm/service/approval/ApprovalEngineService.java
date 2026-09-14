package com.kltn.school_hrm.service.approval;

import java.util.Map;

import com.kltn.school_hrm.dto.response.ApprovalResult;
import com.kltn.school_hrm.entity.approval.ApprovalRequest;
import com.kltn.school_hrm.entity.approval.ApprovalRequestStep;
import com.kltn.school_hrm.entity.employee.Employee;

public interface ApprovalEngineService {

    /**
     * Khởi tạo workflow phê duyệt cho đối tượng nghiệp vụ.
     * Tự động tìm Policy active, đánh giá condition, resolve và lưu các ApprovalRequestStep, ghi nhận action SUBMIT.
     */
    ApprovalRequest initiateRequest(
            String businessType,
            Long businessId,
            String businessRefCode,
            Employee requester,
            Map<String, Object> contextVariables);

    /**
     * Xử lý phê duyệt ở cấp hiện tại.
     * Chuyển cấp duyệt tiếp theo nếu còn; đánh giá hoàn tất (APPROVED) nếu đã duyệt hết các cấp.
     */
    ApprovalResult approve(String businessType, Long businessId, Long approverId, String comment);

    /**
     * Xử lý từ chối ở cấp hiện tại.
     * Ngay lập tức chuyển trạng thái request thành REJECTED.
     */
    ApprovalResult reject(String businessType, Long businessId, Long approverId, String comment);

    /**
     * Hủy yêu cầu phê duyệt (bởi người tạo đơn hoặc Admin).
     */
    ApprovalResult cancel(String businessType, Long businessId, Long actorId, String comment);

    /**
     * Chuyển tiếp / ủy quyền bước duyệt cho người khác (FORWARD).
     */
    void forwardPendingApprovals(Long oldApproverId, Long newApproverId, Long actorId, String comment);

    /**
     * Lấy step hiện tại đang chờ duyệt của request.
     */
    ApprovalRequestStep getCurrentPendingStep(String businessType, Long businessId);
}
