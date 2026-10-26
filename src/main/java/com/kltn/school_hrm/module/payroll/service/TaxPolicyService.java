package com.kltn.school_hrm.module.payroll.service;

import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.module.payroll.dto.request.TaxPolicyRequest;
import com.kltn.school_hrm.module.payroll.dto.response.TaxPolicyResponse;
import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;

/**
 * Service quản lý chính sách thuế TNCN, các mức giảm trừ gia cảnh và biểu thuế lũy tiến.
 * Cho phép Quản trị viên (SUPER_ADMIN) cấu hình linh hoạt mà không cần sửa code.
 */
public interface TaxPolicyService {

    /**
     * Lấy chính sách thuế hiệu lực theo thời điểm cụ thể (hoặc ngày hiện tại nếu targetDate là null).
     */
    TaxPolicyResponse getCurrentPolicy(LocalDate targetDate);

    /**
     * Lấy danh sách toàn bộ các phiên bản chính sách thuế đã lưu trong hệ thống.
     */
    List<TaxPolicyResponse> getAllPolicies();

    /**
     * Xem thông tin chi tiết một chính sách thuế theo ID.
     */
    TaxPolicyResponse getPolicyById(Long id);

    /**
     * Cập nhật chính sách thuế: mức giảm trừ bản thân, người phụ thuộc, tỷ lệ bảo hiểm và các bậc thuế.
     * Chỉ SUPER_ADMIN có quyền thực hiện.
     */
    TaxPolicyResponse updatePolicy(Long id, TaxPolicyRequest request);

    /**
     * Tạo mới một phiên bản chính sách thuế mới theo ngày hiệu lực.
     * Chỉ SUPER_ADMIN có quyền thực hiện.
     */
    TaxPolicyResponse createPolicy(TaxPolicyRequest request);

    /**
     * Lấy đối tượng Entity chính sách thuế active để phục vụ trực tiếp cho engine tính lương.
     */
    TaxPolicy getActivePolicyEntity(LocalDate targetDate);
}
