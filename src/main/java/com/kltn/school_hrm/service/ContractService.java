package com.kltn.school_hrm.service;

import com.kltn.school_hrm.dto.request.ContractRequest;
import com.kltn.school_hrm.dto.response.ContractResponse;

public interface ContractService {
    ContractResponse createInitialContract(ContractRequest request);

    ContractResponse createContract(ContractRequest request);

    ContractResponse updateContract(ContractRequest request);

    ContractResponse deleteConract(ContractRequest request);

    void terminate(Long contractId);

    void expire(Long contractId);

    ContractResponse renew(Long contractId, ContractRequest request);

    List<ContractResponse> getContractHistory(Long employeeId);
}
