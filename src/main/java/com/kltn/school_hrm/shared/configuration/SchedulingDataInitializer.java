package com.kltn.school_hrm.shared.configuration;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.AcademicYearRepository;
import com.kltn.school_hrm.module.academic.repository.ClassroomRepository;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
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
import com.kltn.school_hrm.module.scheduling.entity.TimeSlot;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimeSlotRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableRepository;
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
 * Chạy sau DataInitializer nền tảng (@Order(2)), tự động kiểm tra trùng lặp (idempotent).
 * </p>
 */
@Component
@Order(2)
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== SchedulingDataInitializer: Bắt đầu khởi tạo dữ liệu mẫu Học vụ & Thời khóa biểu ===");

        // 1. Khởi tạo chức vụ & tài khoản Phó hiệu trưởng và các giáo viên bộ môn
        Position vicePrincipalPos = positionRepository.findByCode(PositionCode.VICE_PRINCIPAL).orElseGet(() -> {
            Position pos = Position.builder()
                    .code(PositionCode.VICE_PRINCIPAL)
                    .name("Phó hiệu trưởng")
                    .positionAllowanceRate(new BigDecimal("0.40"))
                    .build();
            return positionRepository.save(pos);
        });

        Role boardRole = roleRepository.findByRoleCode(RoleCode.BOARD_OF_DIRECTORS).orElse(null);
        Role teacherRole = roleRepository.findByRoleCode(RoleCode.TEACHER).orElse(null);
        Position teacherPos = positionRepository.findByCode(PositionCode.TEACHER).orElse(null);

        Department adminDept = departmentRepository.findByCode("BAN-GIAM-HOC").orElse(null);
        Department mathDept = departmentRepository.findByCode("BAN-TOAN").orElse(null);
        Department engDept = departmentRepository.findByCode("BAN-ANH").orElse(null);
        Department physDept = departmentRepository.findByCode("BAN-LY").orElse(null);

        // Seed Vice Principal (Phó hiệu trưởng - có quyền tạo & công bố TKB)
        Employee vicePrincipalEmp = seedEmployee(
                "viceprincipal", "VicePrincipal@123", "EMP-VP-001",
                "Trần Thị Phó Hiệu Trưởng", "viceprincipal@school-hrm.local", "0900000010",
                "Nữ", boardRole, adminDept, vicePrincipalPos
        );

        // Seed Giáo viên Văn, Anh, Lý
        Employee teacherMath = employeeRepository.findByEmployeeCode("EMP-TEACHER-001").orElse(null);
        Employee teacherVan = seedEmployee(
                "teachervan", "Teacher@123", "EMP-T-VAN-001",
                "Lê Thị Ngữ Văn", "van.le@school-hrm.local", "0900000011",
                "Nữ", teacherRole, adminDept, teacherPos
        );
        Employee teacherAnh = seedEmployee(
                "teacheranh", "Teacher@123", "EMP-T-ENG-001",
                "Phạm Văn Tiếng Anh", "anh.pham@school-hrm.local", "0900000012",
                "Nam", teacherRole, engDept, teacherPos
        );
        Employee teacherLy = seedEmployee(
                "teacherly", "Teacher@123", "EMP-T-PHYS-001",
                "Hoàng Văn Vật Lý", "ly.hoang@school-hrm.local", "0900000013",
                "Nam", teacherRole, physDept, teacherPos
        );

        // 2. Khởi tạo Năm học & Học kỳ
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
                    Semester saved = semesterRepository.save(s);
                    log.info("  [+] Đã tạo Học kỳ 1: {}", saved.getName());
                    return saved;
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

        // 3. Khởi tạo Môn học (Subjects)
        Subject subMath = seedSubject("MATH", "Toán học", "Môn Toán chuẩn THPT", SubjectStatus.ACTIVE);
        Subject subLit = seedSubject("LIT", "Ngữ văn", "Môn Ngữ văn chuẩn THPT", SubjectStatus.ACTIVE);
        Subject subEng = seedSubject("ENG", "Tiếng Anh", "Môn Tiếng Anh hệ 10 năm", SubjectStatus.ACTIVE);
        Subject subPhys = seedSubject("PHYS", "Vật lý", "Môn Vật lý", SubjectStatus.ACTIVE);
        Subject subChem = seedSubject("CHEM", "Hóa học", "Môn Hóa học", SubjectStatus.ACTIVE);
        Subject subBio = seedSubject("BIO", "Sinh học", "Môn Sinh học", SubjectStatus.ACTIVE);
        Subject subIT = seedSubject("IT", "Tin học", "Môn Tin học ứng dụng", SubjectStatus.ACTIVE);
        Subject subPE = seedSubject("PE", "Giáo dục thể chất", "Môn Thể dục thể thao", SubjectStatus.ACTIVE);

        // 4. Khởi tạo Phòng học (Classrooms)
        Classroom room101 = seedClassroom("P101", "Phòng học 101", 45, ClassroomType.STANDARD, "Nhà A", 1);
        Classroom room102 = seedClassroom("P102", "Phòng học 102", 45, ClassroomType.STANDARD, "Nhà A", 1);
        Classroom room103 = seedClassroom("P103", "Phòng học 103", 45, ClassroomType.STANDARD, "Nhà A", 1);
        Classroom roomLabIT = seedClassroom("LAB-IT1", "Phòng máy tính 1", 40, ClassroomType.LAB, "Nhà B", 2);
        Classroom roomLabPhys = seedClassroom("LAB-PHYS", "Phòng thí nghiệm Lý", 40, ClassroomType.LAB, "Nhà B", 2);
        Classroom roomSport = seedClassroom("SAN-GDTC", "Sân Thể thao ngoài trời", 100, ClassroomType.GYM, "Khu Thể thao", 1);

        // 5. Khởi tạo Lớp học (School Classes)
        SchoolClass class10A1 = seedSchoolClass("10A1", 10, academicYear, teacherMath, 45, ClassStatus.ACTIVE);
        SchoolClass class10A2 = seedSchoolClass("10A2", 10, academicYear, teacherVan, 45, ClassStatus.ACTIVE);
        SchoolClass class11A1 = seedSchoolClass("11A1", 11, academicYear, teacherLy, 42, ClassStatus.ACTIVE);

        // 6. Khởi tạo 9 Tiết học chuẩn (TimeSlots)
        seedTimeSlots();

        // 7. Khởi tạo Chuyên môn giáo viên (Teacher Qualifications - BR-09)
        if (teacherMath != null && subMath != null) {
            seedQualification(teacherMath, subMath, "Cử nhân Sư phạm Toán");
        }
        if (teacherVan != null && subLit != null) {
            seedQualification(teacherVan, subLit, "Cử nhân Sư phạm Ngữ văn");
        }
        if (teacherAnh != null && subEng != null) {
            seedQualification(teacherAnh, subEng, "Thạc sĩ TESOL, IELTS 8.0");
        }
        if (teacherLy != null && subPhys != null) {
            seedQualification(teacherLy, subPhys, "Cử nhân Sư phạm Vật lý");
        }

        // 8. Khởi tạo Định mức giảng dạy (Teaching Norms - BR-07, BR-08)
        seedTeachingNorm(teacherPos, "2025-2026", 17);

        // 9. Phân công giảng dạy Lớp - Môn (Teaching Assignments - 1 năm học)
        TeachingAssignment asgMath = seedAssignment(class10A1, subMath, teacherMath, "Toán 10", 4.0, Curriculum.MOET);
        TeachingAssignment asgLit = seedAssignment(class10A1, subLit, teacherVan, "Ngữ văn 10", 4.0, Curriculum.MOET);
        TeachingAssignment asgEng = seedAssignment(class10A1, subEng, teacherAnh, "Tiếng Anh 10", 3.0, Curriculum.MOET);
        TeachingAssignment asgPhys = seedAssignment(class10A1, subPhys, teacherLy, "Vật lý 10", 2.0, Curriculum.MOET);

        // 10. Khởi tạo Thời khóa biểu mẫu cho lớp 10A1 (Học kỳ 1)
        seedSampleTimetable(class10A1, semester1, asgMath, asgLit, asgEng, asgPhys, room101, teacherLy);

        log.info("=== SchedulingDataInitializer: Hoàn tất khởi tạo dữ liệu mẫu Học vụ & Thời khóa biểu ===");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────────────────────────────────

    private Employee seedEmployee(String username, String rawPassword, String empCode,
                                  String fullName, String email, String phone,
                                  String gender, Role role, Department dept, Position pos) {
        if (userRepository.existsByUsername(username)) {
            return employeeRepository.findByEmployeeCode(empCode).orElse(null);
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .email(email)
                .phone(phone)
                .status(UserStatus.ACTIVE)
                .role(role)
                .build();
        user = userRepository.save(user);

        Employee emp = Employee.builder()
                .user(user)
                .employeeCode(empCode)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .citizenId("0790000" + empCode.substring(empCode.length() - 5))
                .gender(gender)
                .department(dept)
                .position(pos)
                .joinDate(LocalDate.of(2024, 1, 1))
                .status(EmployeeStatus.WORKING)
                .build();
        emp = employeeRepository.save(emp);
        log.info("  [+] Đã tạo Nhân sự: {} ({}) - username: {}", fullName, empCode, username);
        return emp;
    }

    private Subject seedSubject(String code, String name, String desc, SubjectStatus status) {
        return subjectRepository.findByCode(code).orElseGet(() -> {
            Subject s = Subject.builder()
                    .code(code)
                    .name(name)
                    .description(desc)
                    .status(status)
                    .build();
            return subjectRepository.save(s);
        });
    }

    private Classroom seedClassroom(String code, String name, Integer capacity,
                                   ClassroomType type, String building, Integer floor) {
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
            TeacherSubjectQualification q = TeacherSubjectQualification.builder()
                    .teacher(teacher)
                    .subject(subject)
                    .qualificationLevel(level)
                    .status(SubjectStatus.ACTIVE)
                    .build();
            qualificationRepository.save(q);
        }
    }

    private void seedTeachingNorm(Position pos, String academicYear, int hours) {
        if (pos == null) return;
        if (teachingNormRepository.findByPositionIdAndAcademicYear(pos.getId(), academicYear).isEmpty()) {
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

    private void seedSampleTimetable(SchoolClass clazz, Semester semester,
                                     TeachingAssignment asgMath, TeachingAssignment asgLit,
                                     TeachingAssignment asgEng, TeachingAssignment asgPhys,
                                     Classroom room, Employee substituteTeacher) {
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
        if (slots.size() < 4) return;

        TimeSlot slot1 = slots.get(0);
        TimeSlot slot2 = slots.get(1);
        TimeSlot slot3 = slots.get(2);
        TimeSlot slot4 = slots.get(3);

        // Thứ 2: Tiết 1-2 Toán, Tiết 3-4 Văn
        if (asgMath != null) {
            seedEntry(tt, asgMath, DayOfWeek.MONDAY.getValue(), slot1, room, null);
            seedEntry(tt, asgMath, DayOfWeek.MONDAY.getValue(), slot2, room, null);
        }
        if (asgLit != null) {
            seedEntry(tt, asgLit, DayOfWeek.MONDAY.getValue(), slot3, room, null);
            seedEntry(tt, asgLit, DayOfWeek.MONDAY.getValue(), slot4, room, null);
        }

        // Thứ 3: Tiết 1-2 Tiếng Anh, Tiết 3-4 Vật Lý (có 1 tiết có Giáo viên dạy thay mẫu!)
        if (asgEng != null) {
            seedEntry(tt, asgEng, DayOfWeek.TUESDAY.getValue(), slot1, room, null);
            seedEntry(tt, asgEng, DayOfWeek.TUESDAY.getValue(), slot2, room, null);
        }
        if (asgPhys != null) {
            // Tiết 3 do giáo viên chính dạy
            seedEntry(tt, asgPhys, DayOfWeek.TUESDAY.getValue(), slot3, room, null);
            // Tiết 4 có giáo viên dạy thay mẫu
            seedEntry(tt, asgPhys, DayOfWeek.TUESDAY.getValue(), slot4, room, substituteTeacher);
        }

        // Thứ 4: Tiết 1-2 Toán, Tiết 3-4 Văn
        if (asgMath != null) {
            seedEntry(tt, asgMath, DayOfWeek.WEDNESDAY.getValue(), slot1, room, null);
            seedEntry(tt, asgMath, DayOfWeek.WEDNESDAY.getValue(), slot2, room, null);
        }
        if (asgLit != null) {
            seedEntry(tt, asgLit, DayOfWeek.WEDNESDAY.getValue(), slot3, room, null);
            seedEntry(tt, asgLit, DayOfWeek.WEDNESDAY.getValue(), slot4, room, null);
        }

        log.info("  [+] Đã tạo Thời khóa biểu mẫu (PUBLISHED v1) cho Lớp: {} - Học kỳ: {}", clazz.getName(), semester.getName());
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

