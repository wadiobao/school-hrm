package com.kltn.school_hrm.shared.configuration;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.AcademicYearRepository;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.academic.repository.ClassroomRepository;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
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
import com.kltn.school_hrm.module.scheduling.dto.request.AutoScheduleSettingRequest;
import com.kltn.school_hrm.module.scheduling.entity.TimeSlot;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimeSlotRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableRepository;
import com.kltn.school_hrm.module.scheduling.service.AutoScheduleEngineService;
import com.kltn.school_hrm.module.teaching.entity.TeacherSubjectQualification;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.module.teaching.entity.TeachingNorm;
import com.kltn.school_hrm.module.teaching.repository.TeacherSubjectQualificationRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingNormRepository;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;
import com.kltn.school_hrm.shared.enums.Enums.RoleCode;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;
import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;
import com.kltn.school_hrm.shared.enums.Enums.UserStatus;

import lombok.RequiredArgsConstructor;

/**
 * Khởi tạo dữ liệu mẫu cho Phân hệ Học vụ & Xếp thời khóa biểu.
 * <p>
 * Khởi tạo 10 Ban (Bộ môn) theo Chương trình GDPT, mỗi ban gồm đúng 3 nhân sự:
 * 1 Trưởng bộ môn (HEAD_OF_DEPARTMENT) và 2 Giáo viên (TEACHER).
 * </p>
 */
@Component("schedulingDataInitializer")
@Order(3)
@DependsOn({"roleDataInitializer", "dataInitializer"})
@RequiredArgsConstructor
public class SchedulingDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchedulingDataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ClassSubjectRepository classSubjectRepository;

    private final TimeSlotRepository timeSlotRepository;
    private final TeachingNormRepository teachingNormRepository;
    private final TeacherSubjectQualificationRepository qualificationRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AutoScheduleEngineService autoScheduleEngineService;

    private record DepartmentGroup(Department department, Subject subject, Employee head, Employee teacher1, Employee teacher2) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== SchedulingDataInitializer: Bắt đầu khởi tạo 10 Ban chuyên môn (3 người/ban) & Thời khóa biểu ===");

        // 1. Khởi tạo Roles & Positions cần thiết
        Position vicePrincipalPos = positionRepository.findByCode(PositionCode.VICE_PRINCIPAL).orElseGet(() ->
                positionRepository.save(Position.builder()
                        .code(PositionCode.VICE_PRINCIPAL)
                        .name("Phó hiệu trưởng")
                        .positionAllowanceRate(new BigDecimal("0.40"))
                        .build()));

        Position hodPos = positionRepository.findByCode(PositionCode.HEAD_OF_DEPARTMENT).orElseGet(() ->
                positionRepository.save(Position.builder()
                        .code(PositionCode.HEAD_OF_DEPARTMENT)
                        .name("Trưởng Bộ môn")
                        .positionAllowanceRate(new BigDecimal("0.35"))
                        .build()));

        Position teacherPos = positionRepository.findByCode(PositionCode.TEACHER).orElseGet(() ->
                positionRepository.save(Position.builder()
                        .code(PositionCode.TEACHER)
                        .name("Giáo viên")
                        .positionAllowanceRate(new BigDecimal("0.20"))
                        .build()));

        Role boardRole = roleRepository.findByRoleCode(RoleCode.BOARD_OF_DIRECTORS).orElseGet(() ->
                roleRepository.save(Role.builder()
                        .roleCode(RoleCode.BOARD_OF_DIRECTORS)
                        .roleName("Ban Giám hiệu")
                        .description("Ban Giám hiệu")
                        .build()));

        Role hodRole = roleRepository.findByRoleCode(RoleCode.HEAD_OF_DEPARTMENT).orElseGet(() ->
                roleRepository.save(Role.builder()
                        .roleCode(RoleCode.HEAD_OF_DEPARTMENT)
                        .roleName("Trưởng Bộ môn")
                        .description("Quản lý chuyên môn và phê duyệt cấp bộ môn")
                        .build()));

        Role teacherRole = roleRepository.findByRoleCode(RoleCode.TEACHER).orElseGet(() ->
                roleRepository.save(Role.builder()
                        .roleCode(RoleCode.TEACHER)
                        .roleName("Giáo viên")
                        .description("Giáo viên giảng dạy")
                        .build()));

        Department adminDept = departmentRepository.findByCode("BAN-GIAM-HOC").orElseGet(() ->
                departmentRepository.save(Department.builder()
                        .code("BAN-GIAM-HOC")
                        .name("Ban Giám hiệu")
                        .build()));

        // Seed Phó hiệu trưởng
        seedEmployee(
                "viceprincipal", "VicePrincipal@123", "EMP-VP-001",
                "Trần Thị Phó Hiệu Trưởng", "viceprincipal@school-hrm.local", "0900000010",
                "Nữ", boardRole, adminDept, vicePrincipalPos
        );

        // 2. Khởi tạo 10 Môn học chuẩn theo chương trình GDPT
        Subject subLit = seedSubject("LIT", "Ngữ văn",
                "Rèn luyện kỹ năng đọc, viết, nói, nghe và cảm thụ văn học.", SubjectStatus.ACTIVE);
        Subject subMath = seedSubject("MATH", "Toán",
                "Học về số học, đại số, hình học, đo lường, thống kê và xác suất.", SubjectStatus.ACTIVE);
        Subject subEng = seedSubject("ENG", "Ngoại ngữ 1",
                "Thường là tiếng Anh (hoặc các ngoại ngữ khác tùy điều kiện từng trường).", SubjectStatus.ACTIVE);
        Subject subGdcd = seedSubject("GDCD", "Giáo dục công dân",
                "Giáo dục đạo đức, kỹ năng sống, kinh tế và pháp luật cơ bản.", SubjectStatus.ACTIVE);
        Subject subHistGeo = seedSubject("HIST_GEO", "Lịch sử và Địa lí",
                "Đây là môn tích hợp gồm hai phân môn Lịch sử và Địa lí.", SubjectStatus.ACTIVE);
        Subject subKhtn = seedSubject("KHTN", "Khoa học tự nhiên",
                "Đây là môn tích hợp kiến thức của ba phân môn Vật lí, Hóa học và Sinh học.", SubjectStatus.ACTIVE);
        Subject subTech = seedSubject("TECH", "Công nghệ",
                "Tìm hiểu về kỹ thuật, nông nghiệp, công nghiệp, điện tử và hướng nghiệp.", SubjectStatus.ACTIVE);
        Subject subIt = seedSubject("IT", "Tin học",
                "Học kỹ năng sử dụng máy tính, công nghệ thông tin, an toàn mạng và lập trình cơ bản.", SubjectStatus.ACTIVE);
        Subject subPe = seedSubject("PE", "Giáo dục thể chất",
                "Rèn luyện thể lực và các môn thể thao tự chọn.", SubjectStatus.ACTIVE);
        Subject subArt = seedSubject("ART", "Nghệ thuật",
                "Gồm hai nội dung là Âm nhạc và Mỹ thuật.", SubjectStatus.ACTIVE);

        // 3. Khởi tạo 10 Ban (Bộ môn), mỗi ban gồm đúng 3 người: 1 Trưởng bộ môn + 2 Giáo viên
        // 3.1. Ban Ngữ văn
        DepartmentGroup grpVan = seedAcademicDepartmentWithStaff(
                "BAN-VAN", "Bộ môn Ngữ văn", adminDept, subLit, "Cử nhân SP Ngữ văn",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_van", "Head@123", "EMP-VAN-H01", "ThS. Lê Thị Trưởng Ban Văn", "head.van@school-hrm.local", "0900000101", "Nữ",
                "gv_van1", "Teacher@123", "EMP-VAN-T01", "Cô Nguyễn Thị Ngữ Văn", "gv.van1@school-hrm.local", "0900000102", "Nữ",
                "gv_van2", "Teacher@123", "EMP-VAN-T02", "Thầy Trần Văn Văn Học", "gv.van2@school-hrm.local", "0900000103", "Nam"
        );

        // 3.2. Ban Toán (kế thừa mathhead và teacher từ DataInitializer + bổ sung gv_toan2 để đủ 3 người)
        DepartmentGroup grpToan = seedAcademicDepartmentWithStaff(
                "BAN-TOAN", "Bộ môn Toán", adminDept, subMath, "Cử nhân SP Toán",
                hodRole, hodPos, teacherRole, teacherPos,
                "mathhead", "MathHead@123", "EMP-TBT-001", "ThS. Trần Thị Trưởng Ban Toán", "mathhead@school-hrm.local", "0900000006", "Nữ",
                "teacher", "Teacher@123", "EMP-GV-001", "Thầy Hoàng Văn Giáo Viên", "teacher@school-hrm.local", "0900000004", "Nam",
                "gv_toan2", "Teacher@123", "EMP-TOAN-T02", "Cô Phạm Thị Đại Số", "gv.toan2@school-hrm.local", "0900000203", "Nữ"
        );

        // 3.3. Ban Ngoại ngữ 1
        DepartmentGroup grpNn1 = seedAcademicDepartmentWithStaff(
                "BAN-NN1", "Bộ môn Ngoại ngữ 1", adminDept, subEng, "Cử nhân SP Tiếng Anh",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_nn1", "Head@123", "EMP-NN1-H01", "ThS. Phạm Văn Trưởng Ban Ngoại Ngữ", "head.nn1@school-hrm.local", "0900000301", "Nam",
                "gv_nn1", "Teacher@123", "EMP-NN1-T01", "Cô Đặng Thị Tiếng Anh", "gv.nn1@school-hrm.local", "0900000302", "Nữ",
                "gv_nn2", "Teacher@123", "EMP-NN1-T02", "Thầy Vũ Văn Ngoại Ngữ", "gv.nn2@school-hrm.local", "0900000303", "Nam"
        );

        // 3.4. Ban Giáo dục công dân
        DepartmentGroup grpGdcd = seedAcademicDepartmentWithStaff(
                "BAN-GDCD", "Bộ môn Giáo dục công dân", adminDept, subGdcd, "Cử nhân SP GDCD",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_gdcd", "Head@123", "EMP-GDCD-H01", "ThS. Bùi Văn Trưởng Ban GDCD", "head.gdcd@school-hrm.local", "0900000401", "Nam",
                "gv_gdcd1", "Teacher@123", "EMP-GDCD-T01", "Cô Hoàng Thị Công Dân", "gv.gdcd1@school-hrm.local", "0900000402", "Nữ",
                "gv_gdcd2", "Teacher@123", "EMP-GDCD-T02", "Thầy Ngô Văn Pháp Luật", "gv.gdcd2@school-hrm.local", "0900000403", "Nam"
        );

        // 3.5. Ban Lịch sử và Địa lí
        DepartmentGroup grpSuDia = seedAcademicDepartmentWithStaff(
                "BAN-SUDIA", "Bộ môn Lịch sử và Địa lí", adminDept, subHistGeo, "Cử nhân SP Sử - Địa",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_sudia", "Head@123", "EMP-SD-H01", "ThS. Dương Văn Trưởng Ban Sử Địa", "head.sudia@school-hrm.local", "0900000501", "Nam",
                "gv_sudia1", "Teacher@123", "EMP-SD-T01", "Thầy Lý Văn Lịch Sử", "gv.sudia1@school-hrm.local", "0900000502", "Nam",
                "gv_sudia2", "Teacher@123", "EMP-SD-T02", "Cô Mai Thị Địa Lí", "gv.sudia2@school-hrm.local", "0900000503", "Nữ"
        );

        // 3.6. Ban Khoa học tự nhiên
        DepartmentGroup grpKhtn = seedAcademicDepartmentWithStaff(
                "BAN-KHTN", "Bộ môn Khoa học tự nhiên", adminDept, subKhtn, "Cử nhân SP KHTN",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_khtn", "Head@123", "EMP-KHTN-H01", "TS. Hoàng Văn Trưởng Ban KHTN", "head.khtn@school-hrm.local", "0900000601", "Nam",
                "gv_khtn1", "Teacher@123", "EMP-KHTN-T01", "Thầy Trịnh Văn Vật Lí", "gv.khtn1@school-hrm.local", "0900000602", "Nam",
                "gv_khtn2", "Teacher@123", "EMP-KHTN-T02", "Cô Võ Thị Hóa Sinh", "gv.khtn2@school-hrm.local", "0900000603", "Nữ"
        );

        // 3.7. Ban Công nghệ
        DepartmentGroup grpCn = seedAcademicDepartmentWithStaff(
                "BAN-CN", "Bộ môn Công nghệ", adminDept, subTech, "Cử nhân SP Công nghệ",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_cn", "Head@123", "EMP-CN-H01", "ThS. Đỗ Văn Trưởng Ban Công Nghệ", "head.cn@school-hrm.local", "0900000701", "Nam",
                "gv_cn1", "Teacher@123", "EMP-CN-T01", "Thầy Phan Văn Kỹ Thuật", "gv.cn1@school-hrm.local", "0900000702", "Nam",
                "gv_cn2", "Teacher@123", "EMP-CN-T02", "Cô Lâm Thị Công Nghệ", "gv.cn2@school-hrm.local", "0900000703", "Nữ"
        );

        // 3.8. Ban Tin học
        DepartmentGroup grpTin = seedAcademicDepartmentWithStaff(
                "BAN-TIN", "Bộ môn Tin học", adminDept, subIt, "Cử nhân SP Tin học",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_tin", "Head@123", "EMP-TIN-H01", "ThS. Đinh Văn Trưởng Ban Tin Học", "head.tin@school-hrm.local", "0900000801", "Nam",
                "gv_tin1", "Teacher@123", "EMP-TIN-T01", "Thầy Trương Văn Lập Trình", "gv.tin1@school-hrm.local", "0900000802", "Nam",
                "gv_tin2", "Teacher@123", "EMP-TIN-T02", "Cô Châu Thị Tin Học", "gv.tin2@school-hrm.local", "0900000803", "Nữ"
        );

        // 3.9. Ban Giáo dục thể chất
        DepartmentGroup grpGdtc = seedAcademicDepartmentWithStaff(
                "BAN-GDTC", "Bộ môn Giáo dục thể chất", adminDept, subPe, "Cử nhân GD Thể chất",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_gdtc", "Head@123", "EMP-GDTC-H01", "ThS. Lương Văn Trưởng Ban Thể Chất", "head.gdtc@school-hrm.local", "0900000901", "Nam",
                "gv_gdtc1", "Teacher@123", "EMP-GDTC-T01", "Thầy Hà Văn Thể Thao", "gv.gdtc1@school-hrm.local", "0900000902", "Nam",
                "gv_gdtc2", "Teacher@123", "EMP-GDTC-T02", "Cô Tạ Thị Điền Kinh", "gv.gdtc2@school-hrm.local", "0900000903", "Nữ"
        );

        // 3.10. Ban Nghệ thuật
        DepartmentGroup grpNgheThuat = seedAcademicDepartmentWithStaff(
                "BAN-NGHETHUAT", "Bộ môn Nghệ thuật", adminDept, subArt, "Cử nhân SP Nghệ thuật",
                hodRole, hodPos, teacherRole, teacherPos,
                "head_nghethuat", "Head@123", "EMP-NT-H01", "ThS. Cao Thị Trưởng Ban Nghệ Thuật", "head.nghethuat@school-hrm.local", "0900001001", "Nữ",
                "gv_nt1", "Teacher@123", "EMP-NT-T01", "Cô Đoàn Thị Âm Nhạc", "gv.nt1@school-hrm.local", "0900001002", "Nữ",
                "gv_nt2", "Teacher@123", "EMP-NT-T02", "Thầy Huỳnh Văn Mỹ Thuật", "gv.nt2@school-hrm.local", "0900001003", "Nam"
        );

        // 4. Khởi tạo Năm học & Học kỳ
        AcademicYear academicYear = academicYearRepository.findByName("Năm học 2025-2026").orElseGet(() -> {
            AcademicYear y = AcademicYear.builder()
                    .name("Năm học 2025-2026")
                    .startDate(LocalDate.of(2025, 9, 1))
                    .endDate(LocalDate.of(2026, 6, 30))
                    .status(AcademicStatus.ACTIVE)
                    .build();
            AcademicYear saved = academicYearRepository.save(y);
            log.info("  [+] Đã tạo Năm học: {}", saved.getName());
            return saved;
        });

        Semester semester1 = semesterRepository.findByAcademicYearId(academicYear.getId()).stream()
                .filter(s -> s.getName().contains("Học kỳ 1"))
                .findFirst()
                .orElseGet(() -> {
                    Semester s = Semester.builder()
                            .academicYear(academicYear)
                            .name("Học kỳ 1 (2025-2026)")
                            .startDate(LocalDate.of(2025, 9, 1))
                            .endDate(LocalDate.of(2026, 1, 15))
                            .status(AcademicStatus.ACTIVE)
                            .build();
                    return semesterRepository.save(s);
                });

        semesterRepository.findByAcademicYearId(academicYear.getId()).stream()
                .filter(s -> s.getName().contains("Học kỳ 2"))
                .findFirst()
                .orElseGet(() -> {
                    Semester s = Semester.builder()
                            .academicYear(academicYear)
                            .name("Học kỳ 2 (2025-2026)")
                            .startDate(LocalDate.of(2026, 1, 16))
                            .endDate(LocalDate.of(2026, 6, 30))
                            .status(AcademicStatus.PLANNING)
                            .build();
                    return semesterRepository.save(s);
                });

        // 5. Khởi tạo Phòng học (Classrooms)
        Classroom room101 = seedClassroom("P101", "Phòng học 101", 45, ClassroomType.STANDARD);
        Classroom room102 = seedClassroom("P102", "Phòng học 102", 45, ClassroomType.STANDARD);
        Classroom room103 = seedClassroom("P103", "Phòng học 103", 45, ClassroomType.STANDARD);
        Classroom roomLabIT = seedClassroom("LAB-IT1", "Phòng máy tính 1", 45, ClassroomType.LAB);
        Classroom roomLabKhtn = seedClassroom("LAB-KHTN", "Phòng thí nghiệm KHTN", 45, ClassroomType.LAB);
        Classroom roomSport = seedClassroom("SAN-GDTC", "Sân Thể thao ngoài trời", 100, ClassroomType.GYM);

        // 6. Khởi tạo Lớp học (School Classes)
        SchoolClass class10A1 = seedSchoolClass("10A1", 10, academicYear, grpToan.teacher1(), 45, ClassStatus.ACTIVE);
        SchoolClass class10A2 = seedSchoolClass("10A2", 10, academicYear, grpVan.teacher1(), 45, ClassStatus.ACTIVE);
        SchoolClass class11A1 = seedSchoolClass("11A1", 11, academicYear, grpKhtn.teacher1(), 42, ClassStatus.ACTIVE);

        // 7. Khởi tạo 9 Tiết học chuẩn (TimeSlots)
        seedTimeSlots();

        // 8. Khởi tạo Định mức giảng dạy chuẩn THCS theo Thông tư 05/2025/TT-BGDĐT (Giáo viên: 19 tiết/tuần, Phó HT: 4 tiết/tuần)
        seedTeachingNorm(teacherPos, "2025-2026", 19);
        seedTeachingNorm(hodPos, "2025-2026", 19);
        seedTeachingNorm(vicePrincipalPos, "2025-2026", 4);

        // 9. Tự động phân bổ giáo viên (cân bằng tải) và xếp thời khóa biểu cho tất cả các lớp trong Học kỳ 1 (25 tiết/tuần cho 10 môn văn hóa bắt buộc)
        if (!timetableRepository.existsBySchoolClassIdAndSemesterIdAndVersion(class10A2.getId(), semester1.getId(), 1)) {
            autoScheduleEngineService.generateSemesterSchedule(AutoScheduleSettingRequest.builder()
                    .semesterId(semester1.getId())
                    .activeDays(List.of(1, 2, 3, 4, 5, 6))
                    .sessionMode("MORNING")
                    .maxPeriodsPerDay(5)
                    .maxPeriodsPerSubjectPerDay(2)
                    .targetWeeklyPeriods(25)
                    .autoAssignTeachers(true)
                    .overwriteExisting(true)
                    .autoPublish(true)
                    .build());
        }

        log.info("=== SchedulingDataInitializer: Hoàn tất khởi tạo 10 Ban (30 nhân sự chuyên môn) & Thời khóa biểu ===");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────────────────────────────────

    private DepartmentGroup seedAcademicDepartmentWithStaff(
            String deptCode, String deptName, Department parentDept, Subject subject, String qualificationLevel,
            Role hodRole, Position hodPos, Role teacherRole, Position teacherPos,
            String headUser, String headPass, String headCode, String headName, String headEmail, String headPhone, String headGender,
            String t1User, String t1Pass, String t1Code, String t1Name, String t1Email, String t1Phone, String t1Gender,
            String t2User, String t2Pass, String t2Code, String t2Name, String t2Email, String t2Phone, String t2Gender) {

        Department dept = departmentRepository.findByCode(deptCode).orElseGet(() -> {
            Department d = Department.builder()
                    .code(deptCode)
                    .name(deptName)
                    .parentDepartment(parentDept)
                    .build();
            return departmentRepository.save(d);
        });
        if (!deptName.equals(dept.getName())) {
            dept.setName(deptName);
            dept = departmentRepository.save(dept);
        }

        // 1. Trưởng bộ môn
        Employee head = seedEmployee(headUser, headPass, headCode, headName, headEmail, headPhone, headGender, hodRole, dept, hodPos);
        // 2. Giáo viên 1
        Employee teacher1 = seedEmployee(t1User, t1Pass, t1Code, t1Name, t1Email, t1Phone, t1Gender, teacherRole, dept, teacherPos);
        // 3. Giáo viên 2
        Employee teacher2 = seedEmployee(t2User, t2Pass, t2Code, t2Name, t2Email, t2Phone, t2Gender, teacherRole, dept, teacherPos);

        // Gán Trưởng bộ môn làm Manager của Ban
        if (head != null && (dept.getManager() == null || !head.getId().equals(dept.getManager().getId()))) {
            dept.setManager(head);
            dept = departmentRepository.save(dept);
            log.info("  [+] Bổ nhiệm Trưởng bộ môn '{}': {}", deptName, head.getFullName());
        }

        // Cấp chứng chỉ chuyên môn giảng dạy môn của Ban cho cả 3 người (1 Trưởng + 2 Giáo viên)
        if (subject != null) {
            if (head != null) seedQualification(head, subject, "ThS - " + qualificationLevel);
            if (teacher1 != null) seedQualification(teacher1, subject, qualificationLevel);
            if (teacher2 != null) seedQualification(teacher2, subject, qualificationLevel);
        }

        return new DepartmentGroup(dept, subject, head, teacher1, teacher2);
    }

    private Employee seedEmployee(String username, String rawPassword, String empCode,
                                  String fullName, String email, String phone,
                                  String gender, Role role, Department dept, Position pos) {
        if (role == null) {
            log.error("Role bị null khi seed user '{}'! Không thể tạo tài khoản người dùng.", username);
            return null;
        }

        Optional<Employee> existingByCode = employeeRepository.findByEmployeeCode(empCode);
        if (existingByCode.isPresent()) {
            Employee existing = existingByCode.get();
            boolean changed = false;
            if (dept != null && (existing.getDepartment() == null || !dept.getId().equals(existing.getDepartment().getId()))) {
                existing.setDepartment(dept);
                changed = true;
            }
            if (pos != null && (existing.getPosition() == null || !pos.getId().equals(existing.getPosition().getId()))) {
                existing.setPosition(pos);
                changed = true;
            }
            return changed ? employeeRepository.save(existing) : existing;
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(username);
        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            Optional<Employee> empByUser = employeeRepository.findByUserId(user.getId());
            if (empByUser.isPresent()) {
                return empByUser.get();
            }
        } else {
            user = User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .email(email)
                    .phone(phone)
                    .status(UserStatus.ACTIVE)
                    .role(role)
                    .build();
            user = userRepository.save(user);
        }

        // Tạo số CCCD 12 chữ số duy nhất dựa trên số điện thoại để tránh trùng lặp unique constraint
        String digitsOnlyPhone = phone.replaceAll("\\D", "");
        String uniqueCitizenId = "079" + (digitsOnlyPhone.length() >= 9
                ? digitsOnlyPhone.substring(digitsOnlyPhone.length() - 9)
                : String.format("%09d", Math.abs(empCode.hashCode()) % 1_000_000_000));

        Employee emp = Employee.builder()
                .user(user)
                .employeeCode(empCode)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .citizenId(uniqueCitizenId)
                .gender(gender)
                .department(dept)
                .position(pos)
                .joinDate(LocalDate.of(2024, 1, 1))
                .status(EmployeeStatus.WORKING)
                .build();
        emp = employeeRepository.save(emp);
        log.info("  [+] Đã tạo Nhân sự: {} ({}) - Ban: {} - username: {}",
                fullName, empCode, dept != null ? dept.getName() : "N/A", username);
        return emp;
    }

    private Subject seedSubject(String code, String name, String desc, SubjectStatus status) {
        return subjectRepository.findByCode(code).map(existing -> {
            existing.setName(name);
            existing.setDescription(desc);
            return subjectRepository.save(existing);
        }).orElseGet(() -> {
            Subject s = Subject.builder()
                    .code(code)
                    .name(name)
                    .description(desc)
                    .status(status)
                    .build();
            return subjectRepository.save(s);
        });
    }

    private void seedClassSubject(SchoolClass clazz, Subject subject, Semester semester, int periodsPerWeek) {
        if (clazz == null || subject == null || semester == null) return;
        if (!classSubjectRepository.existsBySchoolClassIdAndSubjectIdAndSemesterId(
                clazz.getId(), subject.getId(), semester.getId())) {
            ClassSubject cs = ClassSubject.builder()
                    .schoolClass(clazz)
                    .subject(subject)
                    .semester(semester)
                    .periodsPerWeek(periodsPerWeek)
                    .build();
            classSubjectRepository.save(cs);
        }
    }

    private Classroom seedClassroom(String code, String name, Integer capacity, ClassroomType type) {
        return classroomRepository.findByCode(code).orElseGet(() -> {
            Classroom c = Classroom.builder()
                    .code(code)
                    .name(name)
                    .capacity(capacity)
                    .roomType(type)
                    .status(ClassroomStatus.AVAILABLE)
                    .build();
            return classroomRepository.save(c);
        });
    }

    private SchoolClass seedSchoolClass(String name, Integer gradeLevel, AcademicYear year,
                                       Employee homeroomTeacher, Integer capacity, ClassStatus status) {
        return schoolClassRepository.findByNameAndAcademicYearId(name, year.getId()).orElseGet(() -> {
            SchoolClass c = SchoolClass.builder()
                    .name(name)
                    .gradeLevel(gradeLevel)
                    .academicYear(year)
                    .homeroomTeacher(homeroomTeacher)
                    .capacity(capacity)
                    .status(status)
                    .build();
            return schoolClassRepository.save(c);
        });
    }

    private void seedTimeSlots() {
        if (timeSlotRepository.count() > 0) return;

        List<TimeSlot> defaultSlots = List.of(
                TimeSlot.builder().session(TimeSlotSession.MORNING).periodNumber(1).startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(7, 45)).build(),
                TimeSlot.builder().session(TimeSlotSession.MORNING).periodNumber(2).startTime(LocalTime.of(7, 50)).endTime(LocalTime.of(8, 35)).build(),
                TimeSlot.builder().session(TimeSlotSession.MORNING).periodNumber(3).startTime(LocalTime.of(8, 50)).endTime(LocalTime.of(9, 35)).build(),
                TimeSlot.builder().session(TimeSlotSession.MORNING).periodNumber(4).startTime(LocalTime.of(9, 40)).endTime(LocalTime.of(10, 25)).build(),
                TimeSlot.builder().session(TimeSlotSession.MORNING).periodNumber(5).startTime(LocalTime.of(10, 30)).endTime(LocalTime.of(11, 15)).build(),
                TimeSlot.builder().session(TimeSlotSession.AFTERNOON).periodNumber(6).startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(13, 45)).build(),
                TimeSlot.builder().session(TimeSlotSession.AFTERNOON).periodNumber(7).startTime(LocalTime.of(13, 50)).endTime(LocalTime.of(14, 35)).build(),
                TimeSlot.builder().session(TimeSlotSession.AFTERNOON).periodNumber(8).startTime(LocalTime.of(14, 50)).endTime(LocalTime.of(15, 35)).build(),
                TimeSlot.builder().session(TimeSlotSession.AFTERNOON).periodNumber(9).startTime(LocalTime.of(15, 40)).endTime(LocalTime.of(16, 25)).build()
        );
        timeSlotRepository.saveAll(defaultSlots);
        log.info("  [+] Đã khởi tạo 9 tiết học chuẩn (5 sáng, 4 chiều)");
    }

    private void seedQualification(Employee teacher, Subject subject, String level) {
        if (!qualificationRepository.existsByTeacherIdAndSubjectId(teacher.getId(), subject.getId())) {
            String safeLevel = (level != null && level.length() > 50) ? level.substring(0, 50) : level;
            TeacherSubjectQualification q = TeacherSubjectQualification.builder()
                    .teacher(teacher)
                    .subject(subject)
                    .qualificationLevel(safeLevel)
                    .status(SubjectStatus.ACTIVE)
                    .build();
            qualificationRepository.save(q);
        }
    }

    private void seedTeachingNorm(Position pos, String academicYear, int hours) {
        if (pos == null) return;
        Optional<TeachingNorm> existingOpt = teachingNormRepository.findByPositionIdAndAcademicYear(pos.getId(), academicYear);
        if (existingOpt.isPresent()) {
            TeachingNorm existing = existingOpt.get();
            if (existing.getStandardHours() == null || existing.getStandardHours() != hours) {
                existing.setStandardHours(hours);
                teachingNormRepository.save(existing);
            }
        } else {
            TeachingNorm norm = TeachingNorm.builder()
                    .position(pos)
                    .academicYear(academicYear)
                    .standardHours(hours)
                    .reductionPercentage(BigDecimal.ZERO)
                    .build();
            teachingNormRepository.save(norm);
        }
    }

    private TeachingAssignment seedAssignment(SchoolClass clazz, Subject subject, Employee teacher,
                                              String subjectName, Double hours, Curriculum curriculum) {
        if (clazz == null || subject == null || teacher == null) return null;

        return teachingAssignmentRepository.findBySchoolClassIdAndSubjectId(clazz.getId(), subject.getId())
                .orElseGet(() -> {
                    TeachingAssignment asg = TeachingAssignment.builder()
                            .schoolClass(clazz)
                            .subject(subject)
                            .teacher(teacher)
                            .status(TeachingAssignmentStatus.ACTIVE)
                            .subjectName(subjectName)
                            .gradeLevel("Khối " + clazz.getGradeLevel())
                            .weeklyContactHours(hours)
                            .curriculum(curriculum)
                            .build();
                    return teachingAssignmentRepository.save(asg);
                });
    }

    private void seedSampleTimetable(
            SchoolClass clazz, Semester semester,
            TeachingAssignment asgMath, TeachingAssignment asgLit,
            TeachingAssignment asgEng, TeachingAssignment asgKhtn,
            TeachingAssignment asgHistGeo, TeachingAssignment asgGdcd,
            TeachingAssignment asgTech, TeachingAssignment asgIt,
            TeachingAssignment asgPe, TeachingAssignment asgArt,
            Classroom roomStandard, Classroom roomLabIT,
            Classroom roomLabKhtn, Classroom roomSport,
            Employee substituteKhtnTeacher) {
        if (clazz == null || semester == null) return;

        if (timetableRepository.existsBySchoolClassIdAndSemesterIdAndVersion(clazz.getId(), semester.getId(), 1)) {
            return;
        }

        Timetable tt = Timetable.builder()
                .schoolClass(clazz)
                .semester(semester)
                .version(1)
                .status(TimetableStatus.PUBLISHED)
                .build();
        tt = timetableRepository.save(tt);

        List<TimeSlot> slots = timeSlotRepository.findAll();
        if (slots.size() < 5) return;

        TimeSlot slot1 = slots.get(0);
        TimeSlot slot2 = slots.get(1);
        TimeSlot slot3 = slots.get(2);
        TimeSlot slot4 = slots.get(3);
        TimeSlot slot5 = slots.get(4);

        // Thứ 2: Tiết 1-2 Toán (2/4), Tiết 3-4 Ngữ văn (2/4), Tiết 5 GDCD (1/1)
        if (asgMath != null) {
            seedEntry(tt, asgMath, DayOfWeek.MONDAY.getValue(), slot1, roomStandard, null);
            seedEntry(tt, asgMath, DayOfWeek.MONDAY.getValue(), slot2, roomStandard, null);
        }
        if (asgLit != null) {
            seedEntry(tt, asgLit, DayOfWeek.MONDAY.getValue(), slot3, roomStandard, null);
            seedEntry(tt, asgLit, DayOfWeek.MONDAY.getValue(), slot4, roomStandard, null);
        }
        if (asgGdcd != null) {
            seedEntry(tt, asgGdcd, DayOfWeek.MONDAY.getValue(), slot5, roomStandard, null);
        }

        // Thứ 3: Tiết 1-2 Ngoại ngữ 1 (2/3), Tiết 3-4 KHTN (2/3 - tiết 4 có GV dạy thay cùng ban), Tiết 5 Công nghệ (1/1)
        if (asgEng != null) {
            seedEntry(tt, asgEng, DayOfWeek.TUESDAY.getValue(), slot1, roomStandard, null);
            seedEntry(tt, asgEng, DayOfWeek.TUESDAY.getValue(), slot2, roomStandard, null);
        }
        if (asgKhtn != null) {
            seedEntry(tt, asgKhtn, DayOfWeek.TUESDAY.getValue(), slot3, roomLabKhtn, null);
            seedEntry(tt, asgKhtn, DayOfWeek.TUESDAY.getValue(), slot4, roomLabKhtn, substituteKhtnTeacher);
        }
        if (asgTech != null) {
            seedEntry(tt, asgTech, DayOfWeek.TUESDAY.getValue(), slot5, roomStandard, null);
        }

        // Thứ 4: Tiết 1-2 Toán (4/4), Tiết 3-4 Ngữ văn (4/4)
        if (asgMath != null) {
            seedEntry(tt, asgMath, DayOfWeek.WEDNESDAY.getValue(), slot1, roomStandard, null);
            seedEntry(tt, asgMath, DayOfWeek.WEDNESDAY.getValue(), slot2, roomStandard, null);
        }
        if (asgLit != null) {
            seedEntry(tt, asgLit, DayOfWeek.WEDNESDAY.getValue(), slot3, roomStandard, null);
            seedEntry(tt, asgLit, DayOfWeek.WEDNESDAY.getValue(), slot4, roomStandard, null);
        }

        // Thứ 5: Tiết 1-2 Lịch sử & Địa lí (2/2), Tiết 3-4 Tin học (2/2)
        if (asgHistGeo != null) {
            seedEntry(tt, asgHistGeo, DayOfWeek.THURSDAY.getValue(), slot1, roomStandard, null);
            seedEntry(tt, asgHistGeo, DayOfWeek.THURSDAY.getValue(), slot2, roomStandard, null);
        }
        if (asgIt != null) {
            seedEntry(tt, asgIt, DayOfWeek.THURSDAY.getValue(), slot3, roomLabIT, null);
            seedEntry(tt, asgIt, DayOfWeek.THURSDAY.getValue(), slot4, roomLabIT, null);
        }

        // Thứ 6: Tiết 1 Ngoại ngữ 1 (3/3), Tiết 2 KHTN (3/3), Tiết 3-4 Giáo dục thể chất (2/2)
        if (asgEng != null) {
            seedEntry(tt, asgEng, DayOfWeek.FRIDAY.getValue(), slot1, roomStandard, null);
        }
        if (asgKhtn != null) {
            seedEntry(tt, asgKhtn, DayOfWeek.FRIDAY.getValue(), slot2, roomLabKhtn, null);
        }
        if (asgPe != null) {
            seedEntry(tt, asgPe, DayOfWeek.FRIDAY.getValue(), slot3, roomSport, null);
            seedEntry(tt, asgPe, DayOfWeek.FRIDAY.getValue(), slot4, roomSport, null);
        }

        // Thứ 7: Tiết 1 Nghệ thuật (1/1)
        if (asgArt != null) {
            seedEntry(tt, asgArt, DayOfWeek.SATURDAY.getValue(), slot1, roomStandard, null);
        }

        log.info("  [+] Đã tạo Thời khóa biểu mẫu 10 môn (PUBLISHED v1, 23 tiết/tuần) cho Lớp: {} - Học kỳ: {}",
                clazz.getName(), semester.getName());
    }

    private void seedEntry(Timetable tt, TeachingAssignment asg, Integer dayOfWeek,
                           TimeSlot slot, Classroom room, Employee substitute) {
        TimetableEntry entry = TimetableEntry.builder()
                .timetable(tt)
                .teachingAssignment(asg)
                .dayOfWeek(dayOfWeek)
                .timeSlot(slot)
                .classroom(room)
                .substituteTeacher(substitute)
                .build();
        timetableEntryRepository.save(entry);
    }
}
