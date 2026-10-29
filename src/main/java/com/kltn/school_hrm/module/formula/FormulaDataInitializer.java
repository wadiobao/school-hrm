package com.kltn.school_hrm.module.formula;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.formula.entity.FormulaDefinition;
import com.kltn.school_hrm.module.formula.repository.FormulaDefinitionRepository;
import com.kltn.school_hrm.shared.enums.Enums.FormulaStatus;

import lombok.RequiredArgsConstructor;

@Component
@Order(20)
@RequiredArgsConstructor
public class FormulaDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FormulaDataInitializer.class);

    private final FormulaDefinitionRepository formulaDefinitionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("OVERTIME_PAY", "Tiền làm thêm giờ",
                "baseSalary / standardWorkHours * overtimeHours * overtimeRate",
                "Công thức overtime: lương giờ × số giờ × hệ số");
        seed("GROSS_SALARY", "Tổng thu nhập (Gross)",
                "baseSalary + allowance + overtimePay",
                "Gross = lương cơ bản + phụ cấp + overtime");
        seed("TAXABLE_INCOME", "Thu nhập chịu thuế",
                "grossSalary - taxExemptIncome",
                "Thu nhập chịu thuế sau khi trừ khoản miễn");
        seed("NET_SALARY", "Lương thực nhận (Net)",
                "grossSalary - insurance - pit",
                "Net = Gross - bảo hiểm - thuế TNCN");
    }

    private void seed(String code, String name, String expression, String note) {
        if (formulaDefinitionRepository.existsByCode(code)) {
            return;
        }
        formulaDefinitionRepository.save(FormulaDefinition.builder()
                .code(code)
                .name(name)
                .expression(expression)
                .version("v1")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .status(FormulaStatus.ACTIVE)
                .note(note)
                .build());
        log.info("  [+] Seed formula: {}", code);
    }
}
