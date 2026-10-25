package com.kltn.school_hrm.module.employee.service;

import java.util.List;

import com.kltn.school_hrm.module.employee.dto.request.ContractRequest;
import com.kltn.school_hrm.module.employee.dto.response.ContractResponse;

public interface ContractService {

    /** Tạo hợp đồng khởi đầu (Thử việc) */
    ContractResponse createInitialContract(ContractRequest request);

    /** Tạo hợp đồng mới cho nhân viên */
    ContractResponse createContract(ContractRequest request);

    /** Gia hạn / Ký mới hợp đồng dựa trên hợp đồng cũ (Tạo record mới, không overwrite) */
    ContractResponse renew(Long contractId, ContractRequest request);

    /** Lấy chi tiết một hợp đồng theo ID */
    ContractResponse getContractById(Long contractId);

    /** Lấy hợp đồng đang có hiệu lực hiện tại của nhân viên */
    ContractResponse getCurrentContract(Long employeeId);

    /** Lấy toàn bộ lịch sử hợp đồng của nhân viên theo thứ tự thời gian mới nhất */
    List<ContractResponse> getContractHistory(Long employeeId);

    /** Chấm dứt hợp đồng (giữ nguyên record để tra cứu lịch sử) */
    void terminate(Long contractId);

    /** Đánh dấu hợp đồng hết hạn */
    void expire(Long contractId);

    /** Cập nhật thông tin hợp đồng (bảo toàn nguyên tắc bất biến cho hợp đồng đã kích hoạt) */
    ContractResponse updateContract(Long id, ContractRequest request);

    /** Xóa hợp đồng nháp/chưa kích hoạt (chặn xóa nếu hợp đồng đã có lịch sử) */
    void deleteContract(Long id);
}
