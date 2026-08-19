package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.SalaryComponentRequest;
import com.kltn.school_hrm.dto.response.SalaryComponentResponse;

public interface SalaryComponentService {
    SalaryComponentResponse create(SalaryComponentRequest request);
    SalaryComponentResponse update(Long id, SalaryComponentRequest request);
    SalaryComponentResponse getById(Long id);
    List<SalaryComponentResponse> getAll();
    void delete(Long id);
}
