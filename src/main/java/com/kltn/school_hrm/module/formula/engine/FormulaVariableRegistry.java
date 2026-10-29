package com.kltn.school_hrm.module.formula.engine;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.formula.dto.response.FormulaVariableResponse;

/**
 * Whitelist biến được phép dùng trong công thức. Không cho truy cập field/method Java.
 */
@Component
public class FormulaVariableRegistry {

    private final Map<String, FormulaVariableResponse> variables = new LinkedHashMap<>();

    public FormulaVariableRegistry() {
        register("baseSalary", "Lương cơ bản", "Decimal", "12000000", "Lương cơ bản theo hợp đồng");
        register("allowance", "Phụ cấp", "Decimal", "500000", "Tổng phụ cấp được cộng vào lương");
        register("overtimeHours", "Giờ làm thêm", "Decimal", "10", "Số giờ overtime trong kỳ");
        register("overtimeRate", "Hệ số overtime", "Decimal", "1.5", "Hệ số nhân giờ làm thêm");
        register("standardWorkHours", "Giờ công chuẩn", "Decimal", "176", "Số giờ công chuẩn của kỳ");
        register("overtimePay", "Tiền làm thêm", "Decimal", "1022727.27", "Kết quả overtime hoặc input sẵn");
        register("grossSalary", "Tổng thu nhập", "Decimal", "13522727.27", "Gross salary");
        register("taxExemptIncome", "Thu nhập miễn thuế", "Decimal", "0", "Khoản miễn thuế");
        register("insurance", "Bảo hiểm", "Decimal", "1260000", "BHXH/BHYT/BHTN người lao động");
        register("pit", "Thuế TNCN", "Decimal", "0", "Thuế thu nhập cá nhân");
        register("workingDays", "Ngày công thực tế", "Decimal", "22", "Ngày công được trả lương");
        register("standardDays", "Ngày công chuẩn", "Decimal", "22", "Ngày công chuẩn của kỳ");
    }

    public boolean isAllowed(String code) {
        return variables.containsKey(code);
    }

    public List<FormulaVariableResponse> listAll() {
        return List.copyOf(variables.values());
    }

    public Map<String, BigDecimal> sampleValues() {
        Map<String, BigDecimal> samples = new LinkedHashMap<>();
        variables.values().forEach(v -> samples.put(v.getCode(), new BigDecimal(v.getSampleValue())));
        return samples;
    }

    private void register(String code, String name, String dataType, String sampleValue, String description) {
        variables.put(code, FormulaVariableResponse.builder()
                .code(code)
                .name(name)
                .dataType(dataType)
                .sampleValue(sampleValue)
                .description(description)
                .build());
    }
}
