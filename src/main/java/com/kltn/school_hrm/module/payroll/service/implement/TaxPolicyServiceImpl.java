package com.kltn.school_hrm.module.payroll.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.payroll.calculator.PITCalculator;
import com.kltn.school_hrm.module.payroll.dto.request.TaxBracketRequest;
import com.kltn.school_hrm.module.payroll.dto.request.TaxPolicyRequest;
import com.kltn.school_hrm.module.payroll.dto.response.TaxBracketResponse;
import com.kltn.school_hrm.module.payroll.dto.response.TaxPolicyResponse;
import com.kltn.school_hrm.module.payroll.entity.TaxBracket;
import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;
import com.kltn.school_hrm.module.payroll.repository.TaxPolicyRepository;
import com.kltn.school_hrm.module.payroll.service.TaxPolicyService;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai quản lý chính sách thuế TNCN động, cho phép Quản trị viên tùy chỉnh
 * các mức giảm trừ gia cảnh và các bậc thuế lũy tiến từng phần theo quy định.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class TaxPolicyServiceImpl implements TaxPolicyService {

    private final TaxPolicyRepository taxPolicyRepository;

    /**
     * Lấy chính sách thuế áp dụng cho ngày chỉ định hoặc ngày hiện tại.
     * Cho phép ghi (read-write transaction) để tự động khởi tạo chính sách mặc định nếu chưa có.
     */
    @Override
    public TaxPolicyResponse getCurrentPolicy(LocalDate targetDate) {
        return mapToResponse(getActivePolicyEntity(targetDate));
    }

    /**
     * Lấy danh sách toàn bộ các chính sách thuế đã định nghĩa.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaxPolicyResponse> getAllPolicies() {
        return taxPolicyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết chính sách thuế theo ID.
     */
    @Override
    @Transactional(readOnly = true)
    public TaxPolicyResponse getPolicyById(Long id) {
        TaxPolicy policy = taxPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chính sách thuế: ID " + id));
        return mapToResponse(policy);
    }

    /**
     * Cập nhật các mức giảm trừ bản thân, người phụ thuộc, tỷ lệ bảo hiểm và biểu thuế lũy tiến.
     * Chỉ SUPER_ADMIN có quyền thực hiện.
     */
    @Override
    public TaxPolicyResponse updatePolicy(Long id, TaxPolicyRequest request) {
        TaxPolicy policy = taxPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chính sách thuế: ID " + id));

        policy.setName(request.getName());
        if (request.getVersion() != null) {
            policy.setVersion(request.getVersion());
        }
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());
        policy.setPersonalDeduction(request.getPersonalDeduction());
        policy.setDependentDeduction(request.getDependentDeduction());
        policy.setInsuranceRate(request.getInsuranceRate());
        policy.setNote(request.getNote());

        // Cập nhật lại các bậc thuế
        if (request.getBrackets() != null && !request.getBrackets().isEmpty()) {
            policy.clearBrackets();
            for (TaxBracketRequest bReq : request.getBrackets()) {
                if (bReq.getToAmount() != null && bReq.getToAmount().compareTo(bReq.getFromAmount()) <= 0) {
                    throw new BusinessException("Ngưỡng đến (" + bReq.getToAmount() 
                            + ") phải lớn hơn ngưỡng từ (" + bReq.getFromAmount() + ") ở bậc " + bReq.getBracketOrder());
                }

                TaxBracket bracket = TaxBracket.builder()
                        .taxPolicy(policy)
                        .bracketOrder(bReq.getBracketOrder())
                        .fromAmount(bReq.getFromAmount())
                        .toAmount(bReq.getToAmount())
                        .taxRate(bReq.getTaxRate())
                        .description(bReq.getDescription())
                        .build();
                policy.addBracket(bracket);
            }
        }

        TaxPolicy saved = taxPolicyRepository.save(policy);
        log.info("SUPER_ADMIN đã cập nhật chính sách thuế {}: Giảm trừ bản thân={}, Giảm trừ người phụ thuộc={}",
                saved.getCode(), saved.getPersonalDeduction(), saved.getDependentDeduction());
        return mapToResponse(saved);
    }

    /**
     * Tạo mới một chính sách thuế.
     */
    @Override
    public TaxPolicyResponse createPolicy(TaxPolicyRequest request) {
        String code = "TAX-VN-" + (request.getVersion() != null ? request.getVersion() : System.currentTimeMillis());
        if (taxPolicyRepository.findByCode(code).isPresent()) {
            throw new BusinessException("Mã chính sách " + code + " đã tồn tại");
        }

        TaxPolicy policy = TaxPolicy.builder()
                .code(code)
                .name(request.getName())
                .version(request.getVersion() != null ? request.getVersion() : "v1")
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .personalDeduction(request.getPersonalDeduction())
                .dependentDeduction(request.getDependentDeduction())
                .insuranceRate(request.getInsuranceRate())
                .isActive(true)
                .note(request.getNote())
                .brackets(new ArrayList<>())
                .build();

        if (request.getBrackets() != null) {
            for (TaxBracketRequest bReq : request.getBrackets()) {
                TaxBracket bracket = TaxBracket.builder()
                        .taxPolicy(policy)
                        .bracketOrder(bReq.getBracketOrder())
                        .fromAmount(bReq.getFromAmount())
                        .toAmount(bReq.getToAmount())
                        .taxRate(bReq.getTaxRate())
                        .description(bReq.getDescription())
                        .build();
                policy.addBracket(bracket);
            }
        }

        TaxPolicy saved = taxPolicyRepository.save(policy);
        log.info("Tạo mới chính sách thuế: {}", saved.getCode());
        return mapToResponse(saved);
    }

    /**
     * Lấy Entity chính sách thuế active phục vụ trực tiếp cho quá trình tính toán lương.
     */
    @Override
    public TaxPolicy getActivePolicyEntity(LocalDate targetDate) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
        return taxPolicyRepository.findActivePolicyForDate(date)
                .or(taxPolicyRepository::findFirstByIsActiveTrueOrderByEffectiveFromDesc)
                .orElseGet(this::createAndSaveDefault2026Policy);
    }

    /**
     * Khởi tạo chính sách thuế 2026 mặc định nếu DB chưa có bản ghi nào.
     */
    private TaxPolicy createAndSaveDefault2026Policy() {
        TaxPolicy defaultPolicy = TaxPolicy.builder()
                .code("TAX-VN-2026")
                .name("Chính sách Thuế TNCN & Giảm trừ Gia cảnh 2026")
                .version("2026-v1")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .personalDeduction(new BigDecimal("15500000"))
                .dependentDeduction(new BigDecimal("6200000"))
                .insuranceRate(new BigDecimal("0.105"))
                .isActive(true)
                .note("Biểu thuế lũy tiến 5 bậc và giảm trừ gia cảnh theo quy định Luật Thuế năm 2026")
                .brackets(new ArrayList<>())
                .build();

        for (TaxBracket b : PITCalculator.createDefault2026Brackets()) {
            defaultPolicy.addBracket(b);
        }

        TaxPolicy saved = taxPolicyRepository.save(defaultPolicy);
        log.info("Khởi tạo tự động chính sách thuế chuẩn 2026 trong DB: {}", saved.getCode());
        return saved;
    }

    /**
     * Chuyển đổi Entity sang Response DTO.
     */
    private TaxPolicyResponse mapToResponse(TaxPolicy p) {
        if (p == null) return null;

        List<TaxBracketResponse> bracketResponses = new ArrayList<>();
        if (p.getBrackets() != null) {
            bracketResponses = p.getBrackets().stream()
                    .sorted(Comparator.comparingInt(TaxBracket::getBracketOrder))
                    .map(b -> TaxBracketResponse.builder()
                            .id(b.getId())
                            .bracketOrder(b.getBracketOrder())
                            .fromAmount(b.getFromAmount())
                            .toAmount(b.getToAmount())
                            .taxRate(b.getTaxRate())
                            .description(b.getDescription())
                            .build())
                    .collect(Collectors.toList());
        }

        return TaxPolicyResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .version(p.getVersion())
                .effectiveFrom(p.getEffectiveFrom())
                .effectiveTo(p.getEffectiveTo())
                .personalDeduction(p.getPersonalDeduction())
                .dependentDeduction(p.getDependentDeduction())
                .insuranceRate(p.getInsuranceRate())
                .isActive(p.getIsActive())
                .note(p.getNote())
                .brackets(bracketResponses)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
