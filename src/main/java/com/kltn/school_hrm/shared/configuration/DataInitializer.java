package com.kltn.school_hrm.shared.configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.core.entity.Department;
import com.kltn.school_hrm.module.core.entity.Position;
import com.kltn.school_hrm.module.core.entity.Role;
import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.module.core.repository.DepartmentRepository;
import com.kltn.school_hrm.module.core.repository.PositionRepository;
import com.kltn.school_hrm.module.core.repository.RoleRepository;
import com.kltn.school_hrm.module.core.repository.UserRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.leave.service.LeaveBalanceService;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.RoleCode;
import com.kltn.school_hrm.shared.enums.Enums.UserStatus;

import lombok.RequiredArgsConstructor;

/**
 * Khởi tạo dữ liệu mặc định khi ứng dụng lần đầu khởi động.
 * <p>
 * Chỉ seed khi chưa có dữ liệu (idempotent – an toàn khi chạy nhiều lần).
 * </p>
 *
 * <b>Tài khoản mặc định:</b>
 * 
 * <pre>
 *   Admin     : username=admin      | password=Admin@123
 *   HR        : username=hrmanager  | password=Hr@123456
 *   Staff     : username=staff      | password=Staff@123
 *   Teacher   : username=teacher    | password=Teacher@123
 *   Principal : username=principal  | password=Principal@123
 *   MathHead  : username=mathhead   | password=MathHead@123
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final DepartmentRepository departmentRepository;
	private final PositionRepository positionRepository;
	private final EmployeeRepository employeeRepository;
	private final PasswordEncoder passwordEncoder;
	private final LeaveBalanceService leaveBalanceService;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		log.info("=== DataInitializer: Kiểm tra & khởi tạo dữ liệu mẫu ===");

		// ── 1. Roles ──────────────────────────────────────────────────────────
		Role adminRole = seedRole(RoleCode.SUPER_ADMIN, "Quản trị hệ thống",
				"Toàn quyền quản trị hệ thống HRM");
		Role hrRole = seedRole(RoleCode.HR_MANAGER, "Trưởng phòng Nhân sự",
				"Quản lý hồ sơ nhân viên, bảng lương, chấm công");
		Role staffRole = seedRole(RoleCode.STAFF, "Nhân viên Hành chính",
				"Xem thông tin cá nhân và bảng công");
		Role teacherRole = seedRole(RoleCode.TEACHER, "Giáo viên",
				"Giảng dạy và ghi nhật ký giờ dạy");
		Role hodRole = seedRole(RoleCode.HEAD_OF_DEPARTMENT, "Trưởng Bộ môn",
				"Phê duyệt đơn nghỉ phép và quản lý bộ môn");
		Role boardRole = seedRole(RoleCode.BOARD_OF_DIRECTORS, "Ban Giám hiệu",
				"Phê duyệt cấp cao và xem báo cáo tổng quan");

		// ── 2. Departments ────────────────────────────────────────────────────
		Department adminDept = seedDepartment("BAN-GIAM-HOC", "Ban Giám hiệu");
		Department hrDept = seedSubDepartment("PHONG-NS", "Phòng Nhân sự", adminDept);
		Department itDept = seedSubDepartment("PHONG-CNTT", "Phòng Công nghệ Thông tin", adminDept);
		Department mathDept = seedSubDepartment("BAN-TOAN", "Bộ môn Toán", adminDept);
		seedSubDepartment("BAN-ANH", "Bộ môn Tiếng Anh", adminDept);
		seedSubDepartment("BAN-LY", "Bộ môn Vật lý", adminDept);

		// ── 3. Positions ──────────────────────────────────────────────────────
		Position principalPos = seedPosition("HIEU-TRUONG", "Hiệu trưởng", new BigDecimal("0.50"));
		Position hrManagerPos = seedPosition("TRUONG-NS", "Trưởng phòng NS", new BigDecimal("0.30"));
		Position hodPos = seedPosition("TRUONG-BO-MON", "Trưởng Bộ môn", new BigDecimal("0.35"));
		Position teacherPos = seedPosition("GIAO-VIEN", "Giáo viên", new BigDecimal("0.20"));
		Position staffPos = seedPosition("NHAN-VIEN", "Nhân viên", new BigDecimal("0.10"));
		Position itAdminPos = seedPosition("IT-ADMIN", "Quản trị hệ thống", new BigDecimal("0.25"));

		// ── 4. Users & Employees ──────────────────────────────────────────────
		Employee adminEmp = seedAdminUser(adminRole, adminDept, itAdminPos);
		Employee hrEmp = seedHrUser(hrRole, hrDept, hrManagerPos);
		Employee staffEmp = seedStaffUser(staffRole, hrDept, staffPos);
		Employee teacherEmp = seedTeacherUser(teacherRole, mathDept, teacherPos);
		Employee principalEmp = seedPrincipalUser(boardRole, adminDept, principalPos);
		Employee mathHeadEmp = seedMathHeadUser(hodRole, mathDept, hodPos);

		// Gán trưởng đơn vị (Department Manager) cho các bộ phận
		if (principalEmp != null && (adminDept.getManager() == null || !principalEmp.getId().equals(adminDept.getManager().getId()))) {
			adminDept.setManager(principalEmp);
			departmentRepository.save(adminDept);
			log.info("  [+] Đã bổ nhiệm Hiệu trưởng làm trưởng đơn vị: {}", adminDept.getName());
		}
		if (hrEmp != null && (hrDept.getManager() == null || !hrEmp.getId().equals(hrDept.getManager().getId()))) {
			hrDept.setManager(hrEmp);
			departmentRepository.save(hrDept);
			log.info("  [+] Đã bổ nhiệm Trưởng phòng NS làm trưởng đơn vị: {}", hrDept.getName());
		}
		if (mathHeadEmp != null && (mathDept.getManager() == null || !mathHeadEmp.getId().equals(mathDept.getManager().getId()))) {
			mathDept.setManager(mathHeadEmp);
			departmentRepository.save(mathDept);
			log.info("  [+] Đã bổ nhiệm Trưởng ban Toán làm trưởng bộ môn: {}", mathDept.getName());
		}

		// ── 5. Khởi tạo quỹ nghỉ phép năm hiện tại cho nhân viên mẫu (12 ngày/năm) ──
		int currentYear = LocalDate.now().getYear();
		List.of(adminEmp, hrEmp, staffEmp, teacherEmp, principalEmp, mathHeadEmp).forEach(emp -> {
			if (emp != null) {
				try {
					leaveBalanceService.initializeBalanceForEmployee(emp, currentYear, BigDecimal.valueOf(12));
				} catch (Exception e) {
					log.warn("Không thể khởi tạo quỹ phép cho nhân viên {}: {}", emp.getEmployeeCode(), e.getMessage());
				}
			}
		});

		log.info("=== DataInitializer: Hoàn tất ===");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Private helper methods
	// ─────────────────────────────────────────────────────────────────────────

	/** Tạo Role nếu chưa tồn tại. */
	private Role seedRole(RoleCode code, String name, String description) {
		return roleRepository.findByRoleCode(code).orElseGet(() -> {
			Role role = Role.builder()
					.roleCode(code)
					.roleName(name)
					.description(description)
					.build();
			Role saved = roleRepository.save(role);
			log.info("  [+] Role tạo mới: {}", code);
			return saved;
		});
	}

	/** Tạo Department nếu chưa tồn tại (theo code). */
	private Department seedDepartment(String code, String name) {
		return departmentRepository.findByCode(code).orElseGet(() -> {
			Department dept = Department.builder()
					.code(code)
					.name(name)
					.build();
			Department saved = departmentRepository.save(dept);
			log.info("  [+] Department tạo mới: {} - {}", code, name);
			return saved;
		});
	}

	/** Tạo Department con nếu chưa tồn tại (theo code). */
	private Department seedSubDepartment(String code, String name, Department parent) {
		return departmentRepository.findByCode(code).orElseGet(() -> {
			Department dept = Department.builder()
					.code(code)
					.name(name)
					.parentDepartment(parent)
					.build();
			Department saved = departmentRepository.save(dept);
			log.info("  [+] Department con tạo mới: {} - {}", code, name);
			return saved;
		});
	}

	/** Tạo Position nếu chưa tồn tại (theo code). */
	private Position seedPosition(String code, String name, BigDecimal allowanceRate) {
		return positionRepository.findByCode(code).orElseGet(() -> {
			Position pos = Position.builder()
					.code(code)
					.name(name)
					.positionAllowanceRate(allowanceRate)
					.build();
			Position saved = positionRepository.save(pos);
			log.info("  [+] Position tạo mới: {} - {}", code, name);
			return saved;
		});
	}

	/**
	 * Seed tài khoản Admin (SUPER_ADMIN). username: admin | password: Admin@123
	 */
	private Employee seedAdminUser(Role role, Department dept, Position pos) {
		if (userRepository.existsByUsername("admin")) {
			log.info("  [=] User 'admin' đã tồn tại, bỏ qua.");
			return employeeRepository.findByEmployeeCode("EMP-ADMIN-001").orElse(null);
		}

		User adminUser = User.builder()
				.username("admin")
				.passwordHash(passwordEncoder.encode("Admin@123"))
				.email("admin@school-hrm.local")
				.phone("0900000001")
				.status(UserStatus.ACTIVE)
				.role(role)
				.build();
		adminUser = userRepository.save(adminUser);

		Employee adminEmp = Employee.builder()
				.user(adminUser)
				.employeeCode("EMP-ADMIN-001")
				.fullName("Quản trị viên Hệ thống")
				.email("admin@school-hrm.local")
				.phone("0900000001")
				.citizenId("079000000001")
				.gender("Nam")
				.department(dept)
				.position(pos)
				.joinDate(LocalDate.of(2024, 1, 1))
				.status(EmployeeStatus.WORKING)
				.build();
		adminEmp = employeeRepository.save(adminEmp);

		log.info("  [+] Admin user tạo thành công: username=admin | password=Admin@123");
		return adminEmp;
	}

	/**
	 * Seed tài khoản HR Manager. username: hrmanager | password: Hr@123456
	 */
	private Employee seedHrUser(Role role, Department dept, Position pos) {
		if (userRepository.existsByUsername("hrmanager")) {
			log.info("  [=] User 'hrmanager' đã tồn tại, bỏ qua.");
			return employeeRepository.findByEmployeeCode("EMP-HR-001").orElse(null);
		}

		User hrUser = User.builder()
				.username("hrmanager")
				.passwordHash(passwordEncoder.encode("Hr@123456"))
				.email("hr@school-hrm.local")
				.phone("0900000002")
				.status(UserStatus.ACTIVE)
				.role(role)
				.build();
		hrUser = userRepository.save(hrUser);

		Employee hrEmp = Employee.builder()
				.user(hrUser)
				.employeeCode("EMP-HR-001")
				.fullName("Nguyễn Thị Nhân Sự")
				.email("hr@school-hrm.local")
				.phone("0900000002")
				.citizenId("079000000002")
				.gender("Nữ")
				.department(dept)
				.position(pos)
				.joinDate(LocalDate.of(2024, 1, 15))
				.status(EmployeeStatus.WORKING)
				.build();
		hrEmp = employeeRepository.save(hrEmp);

		log.info("  [+] HR Manager user tạo thành công: username=hrmanager | password=Hr@123456");
		return hrEmp;
	}

	/**
	 * Seed tài khoản Staff mẫu. username: staff | password: Staff@123
	 */
	private Employee seedStaffUser(Role role, Department dept, Position pos) {
		if (userRepository.existsByUsername("staff")) {
			log.info("  [=] User 'staff' đã tồn tại, bỏ qua.");
			return employeeRepository.findByEmployeeCode("EMP-STAFF-001").orElse(null);
		}

		User staffUser = User.builder()
				.username("staff")
				.passwordHash(passwordEncoder.encode("Staff@123"))
				.email("staff@school-hrm.local")
				.phone("0900000003")
				.status(UserStatus.ACTIVE)
				.role(role)
				.build();
		staffUser = userRepository.save(staffUser);

		Employee staffEmp = Employee.builder()
				.user(staffUser)
				.employeeCode("EMP-STAFF-001")
				.fullName("Trần Văn Nhân Viên")
				.email("staff@school-hrm.local")
				.phone("0900000003")
				.citizenId("079000000003")
				.gender("Nam")
				.department(dept)
				.position(pos)
				.joinDate(LocalDate.of(2024, 3, 1))
				.status(EmployeeStatus.WORKING)
				.build();
		staffEmp = employeeRepository.save(staffEmp);

		log.info("  [+] Staff user tạo thành công: username=staff | password=Staff@123");
		return staffEmp;
	}

	/**
	 * Seed tài khoản Giáo viên mẫu. username: teacher | password: Teacher@123
	 */
	private Employee seedTeacherUser(Role role, Department dept, Position pos) {
		if (userRepository.existsByUsername("teacher")) {
			log.info("  [=] User 'teacher' đã tồn tại, bỏ qua.");
			return employeeRepository.findByEmployeeCode("EMP-GV-001").orElse(null);
		}

		User teacherUser = User.builder()
				.username("teacher")
				.passwordHash(passwordEncoder.encode("Teacher@123"))
				.email("teacher@school-hrm.local")
				.phone("0900000004")
				.status(UserStatus.ACTIVE)
				.role(role)
				.build();
		teacherUser = userRepository.save(teacherUser);

		Employee teacherEmp = Employee.builder()
				.user(teacherUser)
				.employeeCode("EMP-GV-001")
				.fullName("Thầy Hoàng Văn Giáo Viên")
				.email("teacher@school-hrm.local")
				.phone("0900000004")
				.citizenId("079000000004")
				.gender("Nam")
				.department(dept)
				.position(pos)
				.joinDate(LocalDate.of(2023, 9, 1))
				.status(EmployeeStatus.WORKING)
				.build();
		teacherEmp = employeeRepository.save(teacherEmp);

		log.info("  [+] Teacher user tạo thành công: username=teacher | password=Teacher@123");
		return teacherEmp;
	}

	/**
	 * Seed tài khoản Hiệu trưởng (BOARD_OF_DIRECTORS). username: principal |
	 * password: Principal@123
	 */
	private Employee seedPrincipalUser(Role role, Department dept, Position pos) {
		if (userRepository.existsByUsername("principal")) {
			log.info("  [=] User 'principal' đã tồn tại, bỏ qua.");
			return employeeRepository.findByEmployeeCode("EMP-HT-001").orElse(null);
		}

		User principalUser = User.builder()
				.username("principal")
				.passwordHash(passwordEncoder.encode("Principal@123"))
				.email("principal@school-hrm.local")
				.phone("0900000005")
				.status(UserStatus.ACTIVE)
				.role(role)
				.build();
		principalUser = userRepository.save(principalUser);

		Employee principalEmp = Employee.builder()
				.user(principalUser)
				.employeeCode("EMP-HT-001")
				.fullName("TS. Nguyễn Văn Hiệu Trưởng")
				.email("principal@school-hrm.local")
				.phone("0900000005")
				.citizenId("079000000005")
				.gender("Nam")
				.department(dept)
				.position(pos)
				.joinDate(LocalDate.of(2020, 8, 1))
				.status(EmployeeStatus.WORKING)
				.build();
		principalEmp = employeeRepository.save(principalEmp);

		log.info("  [+] Principal user tạo thành công: username=principal | password=Principal@123");
		return principalEmp;
	}

	/**
	 * Seed tài khoản Trưởng ban Toán (HEAD_OF_DEPARTMENT). username: mathhead |
	 * password: MathHead@123
	 */
	private Employee seedMathHeadUser(Role role, Department dept, Position pos) {
		if (userRepository.existsByUsername("mathhead")) {
			log.info("  [=] User 'mathhead' đã tồn tại, bỏ qua.");
			return employeeRepository.findByEmployeeCode("EMP-TBT-001").orElse(null);
		}

		User mathHeadUser = User.builder()
				.username("mathhead")
				.passwordHash(passwordEncoder.encode("MathHead@123"))
				.email("mathhead@school-hrm.local")
				.phone("0900000006")
				.status(UserStatus.ACTIVE)
				.role(role)
				.build();
		mathHeadUser = userRepository.save(mathHeadUser);

		Employee mathHeadEmp = Employee.builder()
				.user(mathHeadUser)
				.employeeCode("EMP-TBT-001")
				.fullName("ThS. Trần Thị Trưởng Ban Toán")
				.email("mathhead@school-hrm.local")
				.phone("0900000006")
				.citizenId("079000000006")
				.gender("Nữ")
				.department(dept)
				.position(pos)
				.joinDate(LocalDate.of(2021, 9, 1))
				.status(EmployeeStatus.WORKING)
				.build();
		mathHeadEmp = employeeRepository.save(mathHeadEmp);

		log.info("  [+] MathHead user tạo thành công: username=mathhead | password=MathHead@123");
		return mathHeadEmp;
	}
}
