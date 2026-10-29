package com.kltn.school_hrm.module.formula.service.implement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.formula.dto.request.FormulaDefinitionRequest;
import com.kltn.school_hrm.module.formula.dto.response.FormulaDefinitionResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaVariableResponse;
import com.kltn.school_hrm.module.formula.engine.FormulaVariableRegistry;
import com.kltn.school_hrm.module.formula.engine.expression.Expression;
import com.kltn.school_hrm.module.formula.entity.FormulaDefinition;
import com.kltn.school_hrm.module.formula.repository.FormulaDefinitionRepository;
import com.kltn.school_hrm.module.formula.service.FormulaDefinitionService;
import com.kltn.school_hrm.module.formula.service.FormulaEngineService;
import com.kltn.school_hrm.shared.enums.Enums.FormulaStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class FormulaDefinitionServiceImpl implements FormulaDefinitionService {

    private final FormulaDefinitionRepository formulaDefinitionRepository;
    private final FormulaEngineService formulaEngineService;
    private final FormulaVariableRegistry variableRegistry;

    @Override
    public FormulaDefinitionResponse create(FormulaDefinitionRequest request) {
        String code = normalizeCode(request.getCode());
        if (formulaDefinitionRepository.existsByCode(code)) {
            throw new BusinessException("Mã công thức đã tồn tại: " + code);
        }
        validateEffectiveRange(request);
        Expression parsed = formulaEngineService.parse(request.getExpression());

        FormulaDefinition entity = FormulaDefinition.builder()
                .code(code)
                .name(request.getName().trim())
                .expression(normalizeExpression(request.getExpression()))
                .version(defaultVersion(request.getVersion()))
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(request.getStatus() != null ? request.getStatus() : FormulaStatus.ACTIVE)
                .note(request.getNote())
                .build();

        return mapToResponse(formulaDefinitionRepository.save(entity), parsed);
    }

    @Override
    public FormulaDefinitionResponse update(Long id, FormulaDefinitionRequest request) {
        FormulaDefinition entity = formulaDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy công thức: ID " + id));

        String code = normalizeCode(request.getCode());
        if (formulaDefinitionRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("Mã công thức đã tồn tại: " + code);
        }
        validateEffectiveRange(request);
        Expression parsed = formulaEngineService.parse(request.getExpression());

        String newExpression = normalizeExpression(request.getExpression());
        String version = defaultVersion(request.getVersion());
        if (!newExpression.equals(entity.getExpression()) && version.equals(entity.getVersion())) {
            version = bumpVersion(entity.getVersion());
        }

        entity.setCode(code);
        entity.setName(request.getName().trim());
        entity.setExpression(newExpression);
        entity.setVersion(version);
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : entity.getStatus());
        entity.setNote(request.getNote());

        return mapToResponse(formulaDefinitionRepository.save(entity), parsed);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaDefinitionResponse getById(Long id) {
        FormulaDefinition entity = formulaDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy công thức: ID " + id));
        return mapToResponse(entity, formulaEngineService.parse(entity.getExpression()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormulaDefinitionResponse> getAll() {
        return formulaDefinitionRepository.findAll().stream()
                .map(entity -> mapToResponse(entity, formulaEngineService.parse(entity.getExpression())))
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!formulaDefinitionRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy công thức: ID " + id);
        }
        formulaDefinitionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormulaVariableResponse> getVariables() {
        return variableRegistry.listAll();
    }

    private void validateEffectiveRange(FormulaDefinitionRequest request) {
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BusinessException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu áp dụng");
        }
    }

    private FormulaDefinitionResponse mapToResponse(FormulaDefinition entity, Expression parsed) {
        Set<String> used = new LinkedHashSet<>();
        parsed.collectVariables(used);
        return FormulaDefinitionResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .expression(entity.getExpression())
                .version(entity.getVersion())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .status(entity.getStatus())
                .note(entity.getNote())
                .usedVariables(new ArrayList<>(used))
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase().replace(' ', '_');
    }

    private String normalizeExpression(String expression) {
        return expression.trim().replaceAll("\\s+", " ");
    }

    private String defaultVersion(String version) {
        if (version == null || version.isBlank()) {
            return "v1";
        }
        return version.trim();
    }

    private String bumpVersion(String current) {
        if (current != null && current.matches("v\\d+")) {
            int next = Integer.parseInt(current.substring(1)) + 1;
            return "v" + next;
        }
        return current + "-next";
    }
}
