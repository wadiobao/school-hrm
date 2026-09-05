package com.kltn.school_hrm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kltn.school_hrm.entity.employee.Contract;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.entity.history.EmployeeStatusHistory;
import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.NotFoundException;
import com.kltn.school_hrm.repository.ContractRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.EmployeeStatusHistoryRepository;
import com.kltn.school_hrm.service.implement.EmployeeLifecycleServiceImpl;

@ExtendWith(MockitoExtension.class)
public class EmployeeLifecycleServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeStatusHistoryRepository employeeStatusHistoryRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private EmployeeLifecycleServiceImpl employeeLifecycleService;

    private Employee employee;
    private Contract probationContract;

    @BeforeEach
    void setUp() {
        // Khởi tạo đối tượng Employee giả lập cho các test case
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        
        // Khởi tạo đối tượng Contract (hợp đồng thử việc) giả lập
        probationContract = new Contract();
        probationContract.setId(1L);
        probationContract.setStatus(ContractStatus.ACTIVE);
    }

    @Test
    void completeProbation_success() {
        // Chuẩn bị dữ liệu (Arrange): Giả lập trạng thái và hành vi của các repository
        employee.setStatus(EmployeeStatus.PROBATION);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.findActiveProbationContractByEmployeeId(1L)).thenReturn(Optional.of(probationContract));

        // Thực thi (Act): Gọi hàm hoàn thành thử việc
        employeeLifecycleService.completeProbation(1L, "Completed successfully");

        // Kiểm tra kết quả (Assert): 
        // 1. Hợp đồng thử việc phải chuyển sang trạng thái TERMINATED
        assertEquals(ContractStatus.TERMINATED, probationContract.getStatus());
        // 2. Trạng thái nhân viên phải chuyển thành WORKING
        assertEquals(EmployeeStatus.WORKING, employee.getStatus());
        // 3. Phải gọi hàm save cho contract và history
        verify(contractRepository).save(probationContract);
        verify(employeeStatusHistoryRepository).save(any(EmployeeStatusHistory.class));
    }

    @Test
    void completeProbation_failsIfEmployeeNotFound() {
        // Chuẩn bị dữ liệu: Trả về rỗng khi tìm kiếm nhân viên
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Kiểm tra kết quả: Phải ném ra lỗi NotFoundException
        assertThrows(NotFoundException.class, () -> 
            employeeLifecycleService.completeProbation(1L, "Completed")
        );
    }

    @Test
    void completeProbation_failsIfNoProbationContract() {
        // Chuẩn bị dữ liệu: Tìm thấy nhân viên nhưng không tìm thấy hợp đồng thử việc đang active
        employee.setStatus(EmployeeStatus.PROBATION);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.findActiveProbationContractByEmployeeId(1L)).thenReturn(Optional.empty());

        // Kiểm tra kết quả: Phải ném ra lỗi BusinessException
        assertThrows(BusinessException.class, () -> 
            employeeLifecycleService.completeProbation(1L, "Completed")
        );
    }

    @Test
    void suspend_success() {
        // Chuẩn bị dữ liệu: Nhân viên đang làm việc
        employee.setStatus(EmployeeStatus.WORKING);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Thực thi: Đình chỉ công tác
        employeeLifecycleService.suspend(1L, "Disciplinary action");

        // Kiểm tra kết quả: Trạng thái đổi thành SUSPENDED và lịch sử được lưu
        assertEquals(EmployeeStatus.SUSPENDED, employee.getStatus());
        verify(employeeStatusHistoryRepository).save(any(EmployeeStatusHistory.class));
    }

    @Test
    void resume_success() {
        // Chuẩn bị dữ liệu: Nhân viên đang bị đình chỉ
        employee.setStatus(EmployeeStatus.SUSPENDED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Thực thi: Khôi phục công việc
        employeeLifecycleService.resume(1L, "Back to work");

        // Kiểm tra kết quả: Trạng thái trở lại WORKING và lịch sử được lưu
        assertEquals(EmployeeStatus.WORKING, employee.getStatus());
        verify(employeeStatusHistoryRepository).save(any(EmployeeStatusHistory.class));
    }

    @Test
    void resign_success() {
        // Chuẩn bị dữ liệu: Nhân viên đang làm việc
        employee.setStatus(EmployeeStatus.WORKING);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Thực thi: Xử lý nghỉ việc
        employeeLifecycleService.resign(1L, "Found new job");

        // Kiểm tra kết quả: Trạng thái đổi thành RESIGNED và lịch sử được lưu
        assertEquals(EmployeeStatus.RESIGNED, employee.getStatus());
        verify(employeeStatusHistoryRepository).save(any(EmployeeStatusHistory.class));
    }

    @Test
    void retire_success() {
        // Chuẩn bị dữ liệu: Nhân viên đang làm việc
        employee.setStatus(EmployeeStatus.WORKING);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Thực thi: Xử lý nghỉ hưu
        employeeLifecycleService.retire(1L, "Age limit reached");

        // Kiểm tra kết quả: Trạng thái đổi thành RETIRED và lịch sử được lưu
        assertEquals(EmployeeStatus.RETIRED, employee.getStatus());
        verify(employeeStatusHistoryRepository).save(any(EmployeeStatusHistory.class));
    }
}
