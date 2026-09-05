package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.UserRegisterRequest;
import com.kltn.school_hrm.dto.response.UserResponse;
import com.kltn.school_hrm.entity.core.Role;
import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.enums.Enums.RoleCode;
import com.kltn.school_hrm.enums.Enums.UserStatus;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.RoleRepository;
import com.kltn.school_hrm.repository.UserRepository;
import com.kltn.school_hrm.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

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
