package com.kltn.school_hrm.shared.utils;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("employeeAccess")
@RequiredArgsConstructor
@Slf4j
public class EmployeeAccess {

	private final EmployeeRepository employeeRepository;

	public boolean isEmployee(Long employeeId) {
		log.info("Check id người dùng");
		return employeeRepository.existsById(employeeId);
	}
}
