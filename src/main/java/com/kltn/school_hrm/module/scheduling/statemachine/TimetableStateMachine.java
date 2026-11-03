package com.kltn.school_hrm.module.scheduling.statemachine;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

/**
 * State Machine quản lý vòng đời và chuyển đổi trạng thái của Thời khóa biểu:
 * DRAFT -> VALIDATING -> REVIEW -> PUBLISHED -> ARCHIVED.
 */
@Component
public class TimetableStateMachine {

    private static final Map<TimetableStatus, Set<TimetableStatus>> VALID_TRANSITIONS = Map.of(
            TimetableStatus.DRAFT, EnumSet.of(TimetableStatus.VALIDATING),
            TimetableStatus.VALIDATING, EnumSet.of(TimetableStatus.REVIEW, TimetableStatus.DRAFT),
            TimetableStatus.REVIEW, EnumSet.of(TimetableStatus.PUBLISHED, TimetableStatus.DRAFT),
            TimetableStatus.PUBLISHED, EnumSet.of(TimetableStatus.ARCHIVED),
            TimetableStatus.ARCHIVED, EnumSet.noneOf(TimetableStatus.class)
    );

    /**
     * Kiểm tra và thực hiện chuyển đổi trạng thái hợp lệ.
     * @param current Trạng thái hiện tại
     * @param target Trạng thái đích muốn chuyển sang
     * @throws BusinessException nếu chuyển đổi không hợp lệ
     */
    public void validateTransition(TimetableStatus current, TimetableStatus target) {
        if (current == null || target == null) {
            throw new BusinessException("Trạng thái nguồn và đích không được để null");
        }

        Set<TimetableStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessException(String.format(
                    "Không thể chuyển đổi trạng thái thời khóa biểu từ '%s' sang '%s'",
                    current.getLabel(), target.getLabel()));
        }
    }

    /**
     * Kiểm tra xem thời khóa biểu ở trạng thái hiện tại có cho phép chỉnh sửa tiết học hay không.
     * Chỉ trạng thái DRAFT mới cho phép thêm, sửa, xóa các tiết học (BR-22).
     */
    public boolean canModifyEntries(TimetableStatus status) {
        return status == TimetableStatus.DRAFT;
    }
}
