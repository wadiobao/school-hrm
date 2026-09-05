package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.SalaryComponentRequest;
import com.kltn.school_hrm.dto.response.SalaryComponentResponse;
import com.kltn.school_hrm.entity.payroll.SalaryComponent;
import com.kltn.school_hrm.repository.SalaryComponentRepository;
import com.kltn.school_hrm.service.SalaryComponentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SalaryComponentServiceImpl implements SalaryComponentService {

    private final SalaryComponentRepository salaryComponentRepository;

    @Override
    @Transactional
    public SalaryComponentResponse create(SalaryComponentRequest request) {
        if (salaryComponentRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Salary component code already exists: " + request.getCode());
        }

        SalaryComponent component = SalaryComponent.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .isTaxable(request.getIsTaxable())
                .formulaExpression(request.getFormulaExpression())
                .build();

        component = salaryComponentRepository.save(component);
        return mapToResponse(component);
    }

    @Override
    @Transactional
    public SalaryComponentResponse update(Long id, SalaryComponentRequest request) {
        SalaryComponent component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary component not found"));

        // Kiểm tra code trùng (ngoại trừ bản ghi hiện tại)
        if (!component.getCode().equals(request.getCode())
                && salaryComponentRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Salary component code already exists: " + request.getCode());
        }

        component.setCode(request.getCode());
        component.setName(request.getName());
        component.setType(request.getType());
        component.setIsTaxable(request.getIsTaxable());
        component.setFormulaExpression(request.getFormulaExpression());

        component = salaryComponentRepository.save(component);
        return mapToResponse(component);
    }

    @Override
    public SalaryComponentResponse getById(Long id) {
        SalaryComponent component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary component not found"));
        return mapToResponse(component);
    }

    @Override
    public List<SalaryComponentResponse> getAll() {
        return salaryComponentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!salaryComponentRepository.existsById(id)) {
            throw new RuntimeException("Salary component not found");
        }
        salaryComponentRepository.deleteById(id);
    }

    private SalaryComponentResponse mapToResponse(SalaryComponent component) {
        return SalaryComponentResponse.builder()
                .id(component.getId())
                .code(component.getCode())
                .name(component.getName())
                .type(component.getType())
                .isTaxable(component.getIsTaxable())
                .formulaExpression(component.getFormulaExpression())
                .build();
    }
}
