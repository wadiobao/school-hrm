package com.kltn.school_hrm.module.core.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.core.dto.request.UserRegisterRequest;
import com.kltn.school_hrm.module.core.dto.response.UserResponse;
import com.kltn.school_hrm.module.core.entity.Role;
import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.shared.enums.Enums.RoleCode;
import com.kltn.school_hrm.shared.enums.Enums.UserStatus;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.shared.utils.JwtService;

import lombok.RequiredArgsConstructor;

import com.kltn.school_hrm.module.core.repository.RoleRepository;
import com.kltn.school_hrm.module.core.repository.UserRepository;
import com.kltn.school_hrm.module.core.service.UserService;

import com.kltn.school_hrm.module.core.dto.request.LoginRequest;
import com.kltn.school_hrm.module.core.dto.response.LoginResponse;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final EmployeeRepository employeeRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Override
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String identifier = request.getIdentifier().trim();
		User user = null;

		// 1. Thử tìm qua username hoặc email
		Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);
		if (userOpt.isPresent()) {
			user = userOpt.get();
		} else {
			// 2. Thử tìm qua mã nhân viên (employeeCode)
			Optional<Employee> empOpt = employeeRepository.findByEmployeeCode(identifier);
			if (empOpt.isPresent() && empOpt.get().getUser() != null) {
				user = empOpt.get().getUser();
			}
		}

		if (user == null) {
			throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác.");
		}

		// 3. Kiểm tra trạng thái tài khoản
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BusinessException(
					"Tài khoản đã bị tạm khóa hoặc ngừng kích hoạt. Vui lòng liên hệ Quản trị viên.");
		}

		// 4. Kiểm tra mật khẩu mã hóa BCrypt
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác.");
		}

		// 5. Tìm thông tin nhân viên gắn với tài khoản
		Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);

		String accessToken = jwtService.generateToken(user);

		return LoginResponse.builder()
				.userId(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.fullName(employee != null ? employee.getFullName() : user.getUsername())
				.roleCode(user.getRole() != null && user.getRole().getRoleCode() != null
						? user.getRole().getRoleCode().name()
						: "STAFF")
				.roleName(user.getRole() != null ? user.getRole().getRoleName() : "Nhân viên")
				.employeeId(employee != null ? employee.getId() : null)
				.employeeCode(employee != null ? employee.getEmployeeCode() : null)
				.accessToken(accessToken)
				.build();
	}

	@Override
	@Transactional
	public UserResponse registerUser(UserRegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already exists!");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already exists!");
		}

		// Lấy Role mặc định là STAFF nếu chưa chọn
		Role defaultRole = roleRepository.findByRoleCode(RoleCode.STAFF)
				.orElseThrow(() -> new ResourceNotFoundException("Default Role not found in database"));

		User user = User.builder()
				.username(request.getUsername())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.email(request.getEmail())
				.phone(request.getPhone())
				.status(UserStatus.ACTIVE)
				.role(defaultRole)
				.build();

		User savedUser = userRepository.save(user);
		return mapToResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
		return mapToResponse(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public UserResponse updateUser(Long id, UserRegisterRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

		if (!user.getUsername().equals(request.getUsername())
				&& userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already exists!");
		}
		if (!user.getEmail().equals(request.getEmail())
				&& userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already exists!");
		}

		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		}

		return mapToResponse(userRepository.save(user));
	}

	@Override
	@Transactional
	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User not found with id: " + id);
		}
		userRepository.deleteById(id);
	}

	private UserResponse mapToResponse(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.phone(user.getPhone())
				.status(user.getStatus().name())
				.roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
				.build();
	}
}
