package com.kltn.school_hrm.module.payroll.statemachine;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

/**
 * Điều phối và kiểm soát các bước chuyển đổi trạng thái của bảng lương (Payroll Lifecycle).
 * Đảm bảo bảng lương chỉ được chuyển trạng thái theo đúng luồng nghiệp vụ đã được phê duyệt (req 2, 3, 4).
 */
@Component
public class PayrollStateMachine {

    /**
     * Ma trận quy định các đích đến hợp lệ từ mỗi trạng thái nguồn.
     * Áp dụng để ngăn chặn nhảy cóc trạng thái (ví dụ: DRAFT nhảy thẳng lên PAID).
     */
    private static final Map<PayrollStatus, Set<PayrollStatus>> TRANSITIONS = Map.of(
            PayrollStatus.DRAFT,
            Set.of(PayrollStatus.CALCULATING),

            PayrollStatus.CALCULATING,
            Set.of(PayrollStatus.REVIEW, PayrollStatus.DRAFT),

            PayrollStatus.REVIEW,
            Set.of(
                    PayrollStatus.CALCULATING,
                    PayrollStatus.DRAFT,
                    PayrollStatus.LOCKED
            ),

            PayrollStatus.LOCKED,
            Set.of(
                    PayrollStatus.REVIEW,
                    PayrollStatus.PAID
            ),

            PayrollStatus.PAID,
            Set.of()
    );

    /**
     * Xác thực tính hợp lệ của việc chuyển từ trạng thái hiện tại sang trạng thái kế tiếp.
     *
     * @param current Trạng thái hiện tại của bảng lương
     * @param next Trạng thái đích muốn chuyển sang
     * @throws BusinessException nếu bước chuyển vi phạm quy tắc vòng đời
     */
    public void validateTransition(PayrollStatus current, PayrollStatus next) {
        if (current == null || next == null) {
            throw new BusinessException("Trạng thái nguồn và trạng thái đích không được để trống");
        }

        if (current == next) {
            // Cho phép giữ nguyên trạng thái nếu cần cập nhật nội dung khác mà không đổi trạng thái
            return;
        }

        Set<PayrollStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new BusinessException(String.format(
                    "Không thể chuyển trạng thái bảng lương từ [%s] sang [%s]. Luồng chuyển đổi không hợp lệ.",
                    current.getLabel(), next.getLabel()
            ));
        }
    }

    /**
     * Kiểm tra trạng thái đích có được phép từ trạng thái hiện tại không mà không ném exception.
     */
    public boolean canTransition(PayrollStatus current, PayrollStatus next) {
        if (current == null || next == null) return false;
        if (current == next) return true;
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(next);
    }
}

