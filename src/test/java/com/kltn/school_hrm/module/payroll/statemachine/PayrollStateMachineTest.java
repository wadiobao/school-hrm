package com.kltn.school_hrm.module.payroll.statemachine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

class PayrollStateMachineTest {

    private PayrollStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new PayrollStateMachine();
    }

    @Test
    @DisplayName("DRAFT chỉ có thể chuyển sang CALCULATING")
    void testDraftTransitions() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.DRAFT, PayrollStatus.CALCULATING));
        assertTrue(stateMachine.canTransition(PayrollStatus.DRAFT, PayrollStatus.CALCULATING));

        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.DRAFT, PayrollStatus.REVIEW));
        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.DRAFT, PayrollStatus.LOCKED));
        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.DRAFT, PayrollStatus.PAID));
    }

    @Test
    @DisplayName("CALCULATING có thể chuyển sang REVIEW hoặc DRAFT khi hủy/lỗi")
    void testCalculatingTransitions() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.CALCULATING, PayrollStatus.REVIEW));
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.CALCULATING, PayrollStatus.DRAFT));

        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.CALCULATING, PayrollStatus.LOCKED));
        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.CALCULATING, PayrollStatus.PAID));
    }

    @Test
    @DisplayName("REVIEW có thể chuyển sang CALCULATING (tính lại), DRAFT (trả về), hoặc LOCKED (chốt)")
    void testReviewTransitions() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.REVIEW, PayrollStatus.CALCULATING));
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.REVIEW, PayrollStatus.DRAFT));
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.REVIEW, PayrollStatus.LOCKED));

        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.REVIEW, PayrollStatus.PAID));
    }

    @Test
    @DisplayName("LOCKED có thể chuyển sang REVIEW (mở khóa đặc biệt) hoặc PAID (thanh toán hoàn tất)")
    void testLockedTransitions() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.LOCKED, PayrollStatus.REVIEW));
        assertDoesNotThrow(() -> stateMachine.validateTransition(PayrollStatus.LOCKED, PayrollStatus.PAID));

        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.LOCKED, PayrollStatus.DRAFT));
        assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.LOCKED, PayrollStatus.CALCULATING));
    }

    @Test
    @DisplayName("PAID là trạng thái bất biến, không thể chuyển sang bất kỳ trạng thái nào")
    void testPaidTransitions() {
        for (PayrollStatus target : PayrollStatus.values()) {
            if (target != PayrollStatus.PAID) {
                assertThrows(BusinessException.class, () -> stateMachine.validateTransition(PayrollStatus.PAID, target));
                assertFalse(stateMachine.canTransition(PayrollStatus.PAID, target));
            }
        }
    }
}

