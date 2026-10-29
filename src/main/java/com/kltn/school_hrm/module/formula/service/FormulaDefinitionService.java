package com.kltn.school_hrm.module.formula.service;

import java.util.List;

import com.kltn.school_hrm.module.formula.dto.request.FormulaDefinitionRequest;
import com.kltn.school_hrm.module.formula.dto.response.FormulaDefinitionResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaVariableResponse;

public interface FormulaDefinitionService {

    FormulaDefinitionResponse create(FormulaDefinitionRequest request);

    FormulaDefinitionResponse update(Long id, FormulaDefinitionRequest request);

    FormulaDefinitionResponse getById(Long id);

    List<FormulaDefinitionResponse> getAll();

    void delete(Long id);

    List<FormulaVariableResponse> getVariables();
}
