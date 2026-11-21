package com.kltn.school_hrm.module.employee.service.implement;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.employee.dto.request.EmployeeCreateRequest;
import com.kltn.school_hrm.module.employee.dto.request.WorkPermitRequest;
import com.kltn.school_hrm.module.employee.dto.response.EmployeeResponse;
import com.kltn.school_hrm.module.employee.dto.response.WorkPermitResponse;
import com.kltn.school_hrm.module.core.entity.Department;
import com.kltn.school_hrm.module.core.entity.Position;
import com.kltn.school_hrm.module.core.entity.Role;
import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.entity.WorkPermitAndVisa;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.RoleCode;
import com.kltn.school_hrm.shared.enums.Enums.UserStatus;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.module.core.repository.DepartmentRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.core.repository.PositionRepository;
import com.kltn.school_hrm.module.core.repository.RoleRepository;
import com.kltn.school_hrm.module.core.repository.UserRepository;
import com.kltn.school_hrm.module.employee.repository.WorkPermitAndVisaRepository;
import com.kltn.school_hrm.module.employee.service.EmployeeService;
import com.kltn.school_hrm.module.leave.service.LeaveBalanceService;
import com.kltn.school_hrm.shared.utils.AesEncryptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EmployeeServiceImpl implements EmployeeService {

	private static final Path FACE_DATASET_DIR = Paths.get("cam-simulation", "dataset");

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final PositionRepository positionRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final WorkPermitAndVisaRepository workPermitAndVisaRepository;
	private final AesEncryptor aesEncryptor;
	private final LeaveBalanceService leaveBalanceService;

	@Override
	@Transactional
	public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
		if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
			throw new IllegalArgumentException("Mã nhân viên đã tồn tại!");
		}
		if (employeeRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email đã tồn tại!");
		}

		if (employeeRepository.existsByCitizenId(request.getCitizenId())) {
			throw new IllegalArgumentException("Căn cước công dân đã tồn tại!");
		}

		Department department = departmentRepository.findById(request.getDepartmentId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Không tìm thấy phòng ban với id: " + request.getDepartmentId()));

		Position position = positionRepository.findById(request.getPositionId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Không tìm thấy chức vụ với id: " + request.getPositionId()));

		User user = resolveOrCreateUserAccount(null, request);

		Employee employee = Employee.builder()
				.employeeCode(request.getEmployeeCode().trim())
				.fullName(request.getFullName().trim())
				.nativeName(request.getNativeName())
				.dateOfBirth(request.getDateOfBirth())
				.gender(request.getGender())
				.email(request.getEmail().trim())
				.phone(request.getPhone())
				.address(request.getAddress())
				.citizenId(request.getCitizenId().trim())
				.department(department)
				.position(position)
				.status(EmployeeStatus.WORKING)
				.joinDate(request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now())
				.user(user)
				.build();

		if (request.getFaceImageBase64() != null && !request.getFaceImageBase64().isBlank()) {
			byte[] imageBytes = decodeBase64Image(request.getFaceImageBase64());
			saveFaceImageToDataset(employee.getEmployeeCode(), imageBytes);
			employee.setFaceImageUrl("/api/v1/employees/face-image/" + employee.getEmployeeCode());
		} else if (hasFaceSampleInDataset(employee.getEmployeeCode())) {
			employee.setFaceImageUrl("/api/v1/employees/face-image/" + employee.getEmployeeCode());
		}

		Employee savedEmployee = employeeRepository.save(employee);

		// Tự động khởi tạo quỹ nghỉ phép năm cho nhân viên mới (chuẩn 12 ngày/năm)
		int currentYear = savedEmployee.getJoinDate() != null ? savedEmployee.getJoinDate().getYear() : java.time.LocalDate.now().getYear();
		try {
			leaveBalanceService.initializeBalanceForEmployee(savedEmployee, currentYear, java.math.BigDecimal.valueOf(12));
		} catch (Exception e) {
			// Không làm fail quá trình tạo nhân viên nếu khởi tạo quỹ phép gặp exception
		}

		return mapToResponse(savedEmployee);
	}

	@Override
	@Transactional
	public EmployeeResponse updateEmployee(Long id, EmployeeCreateRequest request) {
		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + id));

		String oldCode = employee.getEmployeeCode();

		// Kiểm tra trùng employeeCode với bản ghi khác
		if (!employee.getEmployeeCode().equals(request.getEmployeeCode())
				&& employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
			throw new IllegalArgumentException("Mã nhân viên đã tồn tại!");
		}
		// Kiểm tra trùng email với bản ghi khác
		if (!employee.getEmail().equals(request.getEmail())
				&& employeeRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email đã tồn tại!");
		}

		if (!employee.getCitizenId().equals(request.getCitizenId())
				&& employeeRepository.existsByCitizenId(request.getCitizenId())) {
			throw new IllegalArgumentException("Căn cước công dân đã tồn tại!");
		}

		Department department = departmentRepository.findById(request.getDepartmentId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Không tìm thấy phòng ban với id: " + request.getDepartmentId()));

		Position position = positionRepository.findById(request.getPositionId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Không tìm thấy chức vụ với id: " + request.getPositionId()));

		User updatedUser = resolveOrCreateUserAccount(employee.getUser(), request);
		if (updatedUser != null) {
			employee.setUser(updatedUser);
		}

		employee.setEmployeeCode(request.getEmployeeCode().trim());
		employee.setFullName(request.getFullName().trim());
		employee.setNativeName(request.getNativeName());
		employee.setDateOfBirth(request.getDateOfBirth());
		employee.setGender(request.getGender());
		employee.setEmail(request.getEmail().trim());
		employee.setPhone(request.getPhone());
		employee.setAddress(request.getAddress());
		employee.setCitizenId(request.getCitizenId().trim());
		employee.setDepartment(department);
		employee.setPosition(position);
		if (request.getJoinDate() != null) {
			employee.setJoinDate(request.getJoinDate());
		}

		// Nếu đổi mã nhân viên mà đã có ảnh mẫu cũ thì đổi tên file ảnh trong dataset
		if (!oldCode.equals(employee.getEmployeeCode()) && hasFaceSampleInDataset(oldCode)) {
			renameFaceSampleInDataset(oldCode, employee.getEmployeeCode());
			employee.setFaceImageUrl("/api/v1/employees/face-image/" + employee.getEmployeeCode());
		}

		if (request.getFaceImageBase64() != null && !request.getFaceImageBase64().isBlank()) {
			byte[] imageBytes = decodeBase64Image(request.getFaceImageBase64());
			saveFaceImageToDataset(employee.getEmployeeCode(), imageBytes);
			employee.setFaceImageUrl("/api/v1/employees/face-image/" + employee.getEmployeeCode());
		}

		return mapToResponse(employeeRepository.save(employee));
	}

	@Override
	@Transactional
	public void deleteEmployee(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + id);
		}
		employeeRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public EmployeeResponse getEmployeeById(Long id) {
		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + id));
		return mapToResponse(employee);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EmployeeResponse> searchEmployees(Long departmentId, String keyword, Pageable pageable) {
		return employeeRepository.searchEmployees(departmentId, keyword, pageable)
				.map(this::mapToResponse);
	}

	@Override
	@Transactional
	public WorkPermitResponse updateWorkPermit(Long employeeId, WorkPermitRequest request) {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + employeeId));

		WorkPermitAndVisa permit = workPermitAndVisaRepository.findByEmployeeId(employeeId)
				.orElseGet(() -> WorkPermitAndVisa.builder().employee(employee).build());

		permit.setWorkPermitNumber(request.getWorkPermitNumber());
		permit.setWpIssueDate(request.getWpIssueDate());
		permit.setWpExpiryDate(request.getWpExpiryDate());
		permit.setWpStatus(request.getWpStatus());
		permit.setTrcNumber(request.getTrcNumber());
		permit.setTrcExpiryDate(request.getTrcExpiryDate());
		permit.setDocumentScanUrl(request.getDocumentScanUrl());

		WorkPermitAndVisa saved = workPermitAndVisaRepository.save(permit);
		return mapToWorkPermitResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkPermitResponse> getExpiringWorkPermitsAndVisas(int withinDays) {
		LocalDate today = LocalDate.now();
		LocalDate warningDate = today.plusDays(withinDays);

		return workPermitAndVisaRepository.findExpiringPermitsAndVisas(today, warningDate).stream()
				.map(this::mapToWorkPermitResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public EmployeeResponse uploadFaceSample(Long employeeId, byte[] imageBytes) {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + employeeId));

		if (imageBytes == null || imageBytes.length == 0) {
			throw new IllegalArgumentException("Dữ liệu ảnh khuôn mặt không được để trống!");
		}

		saveFaceImageToDataset(employee.getEmployeeCode(), imageBytes);
		employee.setFaceImageUrl("/api/v1/employees/face-image/" + employee.getEmployeeCode());
		Employee saved = employeeRepository.save(employee);
		return mapToResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] getFaceSampleBytes(String employeeCode) {
		if (employeeCode == null || employeeCode.isBlank()) {
			throw new ResourceNotFoundException("Mã nhân viên không hợp lệ");
		}
		Path filePath = resolveFaceSamplePath(employeeCode.trim());
		if (filePath == null || !Files.exists(filePath)) {
			throw new ResourceNotFoundException("Chưa có ảnh khuôn mặt mẫu cho nhân viên: " + employeeCode);
		}
		try {
			return Files.readAllBytes(filePath);
		} catch (IOException e) {
			throw new RuntimeException("Không thể đọc file ảnh khuôn mặt mẫu: " + e.getMessage(), e);
		}
	}

	private User resolveOrCreateUserAccount(User existingUser, EmployeeCreateRequest request) {
		if (request.getUserId() != null) {
			return userRepository.findById(request.getUserId())
					.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với id: " + request.getUserId()));
		}

		String rawUsername = request.getUsername() != null ? request.getUsername().trim() : "";
		String rawPassword = request.getPassword() != null ? request.getPassword().trim() : "";
		String rawRoleCode = request.getRoleCode() != null ? request.getRoleCode().trim() : "";

		if (existingUser != null) {
			boolean changed = false;
			if (!rawUsername.isEmpty() && !rawUsername.equals(existingUser.getUsername())) {
				if (userRepository.existsByUsername(rawUsername)) {
					throw new IllegalArgumentException("Tên đăng nhập '" + rawUsername + "' đã tồn tại!");
				}
				existingUser.setUsername(rawUsername);
				changed = true;
			}
			if (!rawPassword.isEmpty()) {
				existingUser.setPasswordHash(passwordEncoder.encode(rawPassword));
				changed = true;
			}
			if (!rawRoleCode.isEmpty()) {
				Role role = resolveRole(rawRoleCode);
				if (role != null) {
					existingUser.setRole(role);
					changed = true;
				}
			}
			if (request.getEmail() != null && !request.getEmail().trim().equals(existingUser.getEmail())) {
				String newEmail = request.getEmail().trim();
				if (!userRepository.existsByEmail(newEmail)) {
					existingUser.setEmail(newEmail);
					changed = true;
				}
			}
			return changed ? userRepository.save(existingUser) : existingUser;
		}

		// Khi tạo nhân viên mới: nếu nhập username (hoặc chọn Role), tự động tạo tài khoản User đăng nhập
		if (rawUsername.isEmpty() && rawPassword.isEmpty() && rawRoleCode.isEmpty()) {
			return null;
		}

		String usernameToCreate = !rawUsername.isEmpty()
				? rawUsername
				: request.getEmployeeCode().trim().toLowerCase().replace("-", "_");

		if (userRepository.existsByUsername(usernameToCreate)) {
			throw new IllegalArgumentException("Tên đăng nhập '" + usernameToCreate + "' đã tồn tại!");
		}

		Role role = resolveRole(rawRoleCode.isEmpty() ? "TEACHER" : rawRoleCode);
		if (role == null) {
			return null;
		}

		String pwdToEncode = !rawPassword.isEmpty() ? rawPassword : "123456";
		String userEmail = request.getEmail().trim();
		if (userRepository.existsByEmail(userEmail)) {
			userEmail = usernameToCreate + "@school.edu.vn";
		}

		User newUser = User.builder()
				.username(usernameToCreate)
				.passwordHash(passwordEncoder.encode(pwdToEncode))
				.email(userEmail)
				.phone(request.getPhone())
				.role(role)
				.status(UserStatus.ACTIVE)
				.build();

		return userRepository.save(newUser);
	}

	private Role resolveRole(String roleCodeStr) {
		try {
			RoleCode rc = RoleCode.valueOf(roleCodeStr.trim().toUpperCase());
			return roleRepository.findByRoleCode(rc).orElse(null);
		} catch (Exception e) {
			return roleRepository.findByRoleCode(RoleCode.TEACHER).orElse(null);
		}
	}

	private byte[] decodeBase64Image(String base64Input) {
		String cleanBase64 = base64Input.trim();
		int commaIdx = cleanBase64.indexOf(',');
		if (commaIdx >= 0) {
			cleanBase64 = cleanBase64.substring(commaIdx + 1);
		}
		return Base64.getDecoder().decode(cleanBase64);
	}

	private void saveFaceImageToDataset(String employeeCode, byte[] imageBytes) {
		try {
			Files.createDirectories(FACE_DATASET_DIR);
			String safeCode = sanitizeEmployeeCode(employeeCode);
			Path targetJpg = FACE_DATASET_DIR.resolve(safeCode + ".jpg");

			// Thử chuyển đổi ảnh (PNG/JPEG/WebP) sang chuẩn RGB JPEG để OpenCV & DeepFace đọc chính xác 100%
			BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
			if (inputImage != null) {
				BufferedImage rgbImage = new BufferedImage(
						inputImage.getWidth(),
						inputImage.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				rgbImage.createGraphics().drawImage(inputImage, 0, 0, Color.WHITE, null);
				ImageIO.write(rgbImage, "jpg", targetJpg.toFile());
			} else {
				Files.write(targetJpg, imageBytes);
			}

			clearDeepFacePickleCache();
			log.info("Đã lưu ảnh khuôn mặt mẫu cho nhân viên {} tại {}", employeeCode, targetJpg.toAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi lưu ảnh khuôn mặt mẫu vào dataset: " + e.getMessage(), e);
		}
	}

	private void renameFaceSampleInDataset(String oldCode, String newCode) {
		try {
			Path oldPath = resolveFaceSamplePath(oldCode);
			if (oldPath != null && Files.exists(oldPath)) {
				Path newPath = FACE_DATASET_DIR.resolve(sanitizeEmployeeCode(newCode) + ".jpg");
				Files.move(oldPath, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				clearDeepFacePickleCache();
			}
		} catch (IOException e) {
			log.warn("Không thể đổi tên file ảnh khuôn mặt từ {} sang {}: {}", oldCode, newCode, e.getMessage());
		}
	}

	private void clearDeepFacePickleCache() {
		if (!Files.exists(FACE_DATASET_DIR)) {
			return;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(FACE_DATASET_DIR, "*.pkl")) {
			for (Path pkl : stream) {
				Files.deleteIfExists(pkl);
				log.info("Đã xóa cache vector khuôn mặt DeepFace để nạp nhân sự mới: {}", pkl.getFileName());
			}
		} catch (IOException e) {
			log.warn("Không thể xóa file cache .pkl của DeepFace: {}", e.getMessage());
		}
	}

	private boolean hasFaceSampleInDataset(String employeeCode) {
		Path path = resolveFaceSamplePath(employeeCode);
		return path != null && Files.exists(path);
	}

	private Path resolveFaceSamplePath(String employeeCode) {
		if (employeeCode == null || employeeCode.isBlank()) {
			return null;
		}
		String safeCode = sanitizeEmployeeCode(employeeCode);
		for (String ext : List.of(".jpg", ".jpeg", ".png")) {
			Path candidate = FACE_DATASET_DIR.resolve(safeCode + ext);
			if (Files.exists(candidate)) {
				return candidate;
			}
		}
		return FACE_DATASET_DIR.resolve(safeCode + ".jpg");
	}

	private String sanitizeEmployeeCode(String employeeCode) {
		return employeeCode.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
	}

	private EmployeeResponse mapToResponse(Employee employee) {
		WorkPermitResponse workPermitResp = null;
		if (employee.getWorkPermitAndVisa() != null) {
			workPermitResp = mapToWorkPermitResponse(employee.getWorkPermitAndVisa());
		}

		boolean hasFace = hasFaceSampleInDataset(employee.getEmployeeCode());
		String faceUrl = hasFace
				? "/api/v1/employees/face-image/" + employee.getEmployeeCode()
				: employee.getFaceImageUrl();

		User u = employee.getUser();

		return EmployeeResponse.builder()
				.id(employee.getId())
				.employeeCode(employee.getEmployeeCode())
				.fullName(employee.getFullName())
				.nativeName(employee.getNativeName())
				.dateOfBirth(employee.getDateOfBirth())
				.gender(employee.getGender())
				.email(employee.getEmail())
				.phone(employee.getPhone())
				.address(employee.getAddress())
				.citizenId(employee.getCitizenId())
				.departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
				.departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
				.positionId(employee.getPosition() != null ? employee.getPosition().getId() : null)
				.positionName(employee.getPosition() != null ? employee.getPosition().getName() : null)
				.status(employee.getStatus())
				.joinDate(employee.getJoinDate())
				.workPermitAndVisa(workPermitResp)
				.userId(u != null ? u.getId() : null)
				.username(u != null ? u.getUsername() : null)
				.roleCode(u != null && u.getRole() != null ? u.getRole().getRoleCode().name() : null)
				.hasFaceSample(hasFace)
				.faceImageUrl(faceUrl)
				.build();
	}

	private WorkPermitResponse mapToWorkPermitResponse(WorkPermitAndVisa permit) {
		return WorkPermitResponse.builder()
				.id(permit.getId())
				.workPermitNumber(permit.getWorkPermitNumber())
				.wpIssueDate(permit.getWpIssueDate())
				.wpExpiryDate(permit.getWpExpiryDate())
				.wpStatus(permit.getWpStatus())
				.trcNumber(permit.getTrcNumber())
				.trcExpiryDate(permit.getTrcExpiryDate())
				.documentScanUrl(permit.getDocumentScanUrl())
				.build();
	}
}
