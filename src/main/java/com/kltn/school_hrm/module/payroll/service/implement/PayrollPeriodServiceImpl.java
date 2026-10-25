package com.kltn.school_hrm.module.payroll.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.payroll.dto.request.PayrollPeriodRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollPeriodResponse;
import com.kltn.school_hrm.module.payroll.entity.PayrollPeriod;
import com.kltn.school_hrm.module.payroll.repository.PayrollPeriodRepository;
import com.kltn.school_hrm.module.payroll.service.PayrollPeriodService;
import com.kltn.school_hrm.shared.enums.Enums.PayrollPeriodStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private final PayrollPeriodRepository periodRepository;

    @Override
    public PayrollPeriodResponse createPeriod(PayrollPeriodRequest request) {
        if (periodRepository.existsByMonthAndYear(request.getMonth(), request.getYear())) {
            throw new BusinessException("Kỳ lương tháng " + request.getMonth()
                    + "/" + request.getYear() + " đã tồn tại");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        PayrollPeriod period = PayrollPeriod.builder()
                .month(request.getMonth())
                .year(request.getYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .standardWorkDays(request.getStandardWorkDays())
                .note(request.getNote())
                .status(PayrollPeriodStatus.OPEN)
                .build();

        return toResponse(periodRepository.save(period));
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPeriodResponse getPeriodById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPeriodResponse> getAllPeriods() {
        return periodRepository.findAllByOrderByYearDescMonthDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PayrollPeriodResponse lockPeriod(Long id) {
        PayrollPeriod period = getOrThrow(id);
        period.lock();
        return toResponse(periodRepository.save(period));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private PayrollPeriod getOrThrow(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy kỳ lương: " + id));
    }

    private PayrollPeriodResponse toResponse(PayrollPeriod p) {
        return PayrollPeriodResponse.builder()
                .id(p.getId())
                .month(p.getMonth())
                .year(p.getYear())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .standardWorkDays(p.getStandardWorkDays())
                .status(p.getStatus())
                .note(p.getNote())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
