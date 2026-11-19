package com.kltn.school_hrm.module.scheduling.service.implement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.academic.repository.ClassroomRepository;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.scheduling.constraint.TimetableValidator;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationResult;
import com.kltn.school_hrm.module.scheduling.dto.request.AutoScheduleSettingRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.AutoScheduleResultResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.AutoScheduleResultResponse.ClassScheduleSummary;
import com.kltn.school_hrm.module.scheduling.dto.response.AutoScheduleResultResponse.TeacherLoadSummary;
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
import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;
import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai thuật toán tự động phân bổ giáo viên & xếp thời khóa biểu toàn trường theo học kỳ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoScheduleEngineServiceImpl implements AutoScheduleEngineService {

    private final SemesterRepository semesterRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final TeacherSubjectQualificationRepository qualificationRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingNormRepository teachingNormRepository;
    private final EmployeeRepository employeeRepository;
    private final TimetableValidator timetableValidator;

    /**
     * Trọng số mặc định của 10 môn học bắt buộc cấp THCS theo Chương trình GDPT 2018 & Công văn 4567/BGDĐT-GDPT:
     * - Ngữ văn (LIT): 4 tiết/tuần (140 tiết/năm)
     * - Toán (MATH): 4 tiết/tuần (140 tiết/năm)
     * - Khoa học tự nhiên (KHTN): 4 tiết/tuần (140 tiết/năm)
     * - Ngoại ngữ 1 (ENG): 3 tiết/tuần (105 tiết/năm)
     * - Lịch sử và Địa lí (HIST_GEO): 3 tiết/tuần (105 tiết/năm)
     * - Giáo dục thể chất (PE): 2 tiết/tuần (70 tiết/năm)
     * - Nghệ thuật (ART): 2 tiết/tuần (70 tiết/năm)
     * - Giáo dục công dân (GDCD): 1 tiết/tuần (35 tiết/năm)
     * - Tin học (IT): 1 tiết/tuần (35 tiết/năm)
     * - Công nghệ (TECH): 1 tiết/tuần (35 tiết/năm; Lớp 8, 9 có 1 kỳ 2 tiết/tuần)
     * Tổng cộng 10 môn văn hóa cốt lõi = 25 tiết/tuần.
     */
    private static final Map<String, Double> DEFAULT_SUBJECT_WEIGHTS = Map.of(
            "LIT", 4.0,
            "MATH", 4.0,
            "KHTN", 4.0,
            "ENG", 3.0,
            "HIST_GEO", 3.0,
            "PE", 2.0,
            "ART", 2.0,
            "GDCD", 1.0,
            "IT", 1.0,
            "TECH", 1.0
    );

    public static final int DEFAULT_STANDARD_NORM_THCS = 19;
    public static final int WEEKS_PER_YEAR = 35;
    public static final int MAX_PAID_OVERTIME_YEARLY = 200;
    public static final double MAX_WEEKLY_OVERTIME_50_PCT = 9.5;

    private static class PlannedCell {
        SchoolClass schoolClass;
        Subject subject;
        TeachingAssignment assignment;
        Employee teacher;
        Classroom classroom;
        int dayOfWeek;
        TimeSlot timeSlot;
    }

    private record TeacherNormProfile(
            int standardNorm,
            int reductionHours,
            String reductionDetails,
            int effectiveNorm
    ) {}

    private record SchedulingTask(
            SchoolClass schoolClass,
            Subject subject,
            TeachingAssignment assignment,
            int periodsRequired,
            int priorityScore
    ) {}

    @Override
    @Transactional
    public AutoScheduleResultResponse generateSemesterSchedule(AutoScheduleSettingRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Kiểm tra Học kỳ & Năm học
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + request.getSemesterId()));
        AcademicYear academicYear = semester.getAcademicYear();
        if (academicYear == null) {
            throw new BusinessException("Học kỳ chưa được gắn với năm học hợp lệ");
        }

        // 2. Lấy danh sách các lớp đang hoạt động trong năm học
        List<SchoolClass> activeClasses = schoolClassRepository.findByAcademicYearId(academicYear.getId()).stream()
                .filter(c -> c.getStatus() == ClassStatus.ACTIVE)
                .sorted(Comparator.comparing(SchoolClass::getGradeLevel).thenComparing(SchoolClass::getName))
                .collect(Collectors.toList());

        if (activeClasses.isEmpty()) {
            throw new BusinessException("Không có lớp học nào đang hoạt động (ACTIVE) trong năm học " + academicYear.getName());
        }

        // 3. Chuẩn hóa cấu hình ngày học & tiết học trong ngày
        List<Integer> activeDays = (request.getActiveDays() != null && !request.getActiveDays().isEmpty())
                ? request.getActiveDays().stream().filter(d -> d >= 1 && d <= 6).distinct().sorted().collect(Collectors.toList())
                : List.of(1, 2, 3, 4, 5, 6);

        if (activeDays.isEmpty()) {
            throw new BusinessException("Phải chọn ít nhất 1 ngày học trong tuần (Thứ 2 - Thứ 7)");
        }

        String sessionMode = (request.getSessionMode() != null && !request.getSessionMode().isBlank())
                ? request.getSessionMode().trim().toUpperCase()
                : "MORNING";

        int maxPeriodsPerDay = (request.getMaxPeriodsPerDay() != null && request.getMaxPeriodsPerDay() > 0)
                ? request.getMaxPeriodsPerDay()
                : ("BOTH".equals(sessionMode) ? 7 : 5);

        List<TimeSlot> allSlots = timeSlotRepository.findAll().stream()
                .sorted(Comparator.comparing(TimeSlot::getPeriodNumber))
                .collect(Collectors.toList());

        if (allSlots.isEmpty()) {
            throw new BusinessException("Chưa có danh mục khung giờ / tiết học (TimeSlot). Vui lòng khởi tạo khung giờ trước.");
        }

        List<TimeSlot> candidateSlots = allSlots.stream()
                .filter(ts -> {
                    if ("MORNING".equals(sessionMode)) return ts.getSession() == TimeSlotSession.MORNING;
                    if ("AFTERNOON".equals(sessionMode)) return ts.getSession() == TimeSlotSession.AFTERNOON;
                    return true;
                })
                .limit(maxPeriodsPerDay)
                .collect(Collectors.toList());

        if (candidateSlots.isEmpty()) {
            candidateSlots = allSlots.stream().limit(maxPeriodsPerDay).collect(Collectors.toList());
        }

        int weeklySlotCapacity = activeDays.size() * candidateSlots.size();
        int targetWeeklyPeriods = (request.getTargetWeeklyPeriods() != null && request.getTargetWeeklyPeriods() > 0)
                ? request.getTargetWeeklyPeriods()
                : 25;

        if (targetWeeklyPeriods > weeklySlotCapacity) {
            throw new BusinessException(String.format(
                    "Tổng số tiết mục tiêu/tuần (%d tiết) vượt quá quỹ tiết khả dụng (%d ngày x %d tiết/ngày = %d tiết)",
                    targetWeeklyPeriods, activeDays.size(), candidateSlots.size(), weeklySlotCapacity));
        }

        int maxPerSubjectPerDay = (request.getMaxPeriodsPerSubjectPerDay() != null && request.getMaxPeriodsPerSubjectPerDay() > 0)
                ? request.getMaxPeriodsPerSubjectPerDay()
                : 2;

        boolean autoAssignTeachers = request.getAutoAssignTeachers() == null || request.getAutoAssignTeachers();
        boolean overwriteExisting = request.getOverwriteExisting() == null || request.getOverwriteExisting();
        boolean autoPublish = request.getAutoPublish() == null || request.getAutoPublish();

        // 4. PHA 1A: Tính toán phân bổ số tiết mỗi môn bằng phương pháp Hamilton (Largest Remainder Method)
        List<Subject> activeSubjects = subjectRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubjectStatus.ACTIVE)
                .sorted(Comparator.comparing(Subject::getCode))
                .collect(Collectors.toList());

        if (activeSubjects.isEmpty()) {
            throw new BusinessException("Không có môn học nào đang hoạt động (ACTIVE) trong hệ thống");
        }

        Map<String, Double> inputRatios = (request.getSubjectRatios() != null && !request.getSubjectRatios().isEmpty())
                ? request.getSubjectRatios()
                : DEFAULT_SUBJECT_WEIGHTS;

        Map<Subject, Integer> allocatedSubjectPeriods = calculateHamiltonSubjectPeriods(
                activeSubjects, inputRatios, targetWeeklyPeriods);

        Map<String, Integer> allocatedByCode = new LinkedHashMap<>();
        for (Map.Entry<Subject, Integer> entry : allocatedSubjectPeriods.entrySet()) {
            if (entry.getValue() > 0) {
                allocatedByCode.put(entry.getKey().getCode() + " - " + entry.getKey().getName(), entry.getValue());
            }
        }

        // Cập nhật cấu hình ClassSubject cho toàn bộ các lớp trong học kỳ
        for (SchoolClass clazz : activeClasses) {
            for (Map.Entry<Subject, Integer> sp : allocatedSubjectPeriods.entrySet()) {
                Subject subj = sp.getKey();
                int periods = sp.getValue();
                Optional<ClassSubject> existingCs = classSubjectRepository
                        .findBySchoolClassIdAndSubjectIdAndSemesterId(clazz.getId(), subj.getId(), semester.getId());
                if (periods > 0) {
                    if (existingCs.isPresent()) {
                        ClassSubject cs = existingCs.get();
                        cs.setPeriodsPerWeek(periods);
                        classSubjectRepository.save(cs);
                    } else {
                        classSubjectRepository.save(ClassSubject.builder()
                                .schoolClass(clazz)
                                .subject(subj)
                                .semester(semester)
                                .periodsPerWeek(periods)
                                .build());
                    }
                } else existingCs.ifPresent(classSubjectRepository::delete);
            }
        }

        // 5. PHA 1B: Công thức tự động phân công Giáo viên theo Thông tư 05/2025/TT-BGDĐT (ĐM_thực = 19 - Giảm trừ kiêm nhiệm)
        Map<Long, TeacherNormProfile> teacherNormProfiles = loadTeacherNormProfiles(academicYear.getName(), activeClasses);
        Map<Long, Integer> teacherRunningPeriods = new HashMap<>();
        Map<Long, Integer> teacherRunningClasses = new HashMap<>();
        Map<Long, Employee> trackedTeachers = new HashMap<>();

        // Khởi tạo danh sách giáo viên có chuyên môn cho từng môn
        Map<Long, List<Employee>> qualifiedTeachersBySubject = new HashMap<>();
        for (Subject subj : activeSubjects) {
            List<Employee> qualified = qualificationRepository.findBySubjectId(subj.getId()).stream()
                    .filter(q -> q.getStatus() == SubjectStatus.ACTIVE
                            && q.getTeacher() != null
                            && q.getTeacher().getStatus() == EmployeeStatus.WORKING)
                    .map(TeacherSubjectQualification::getTeacher)
                    .collect(Collectors.toList());

            qualifiedTeachersBySubject.put(subj.getId(), qualified);
            for (Employee t : qualified) {
                trackedTeachers.put(t.getId(), t);
                teacherRunningPeriods.putIfAbsent(t.getId(), 0);
                teacherRunningClasses.putIfAbsent(t.getId(), 0);
            }
        }

        int totalAssignmentsAllocated = 0;
        Map<String, TeachingAssignment> assignmentMap = new HashMap<>(); // key: classId + ":" + subjectId

        for (SchoolClass clazz : activeClasses) {
            for (Map.Entry<Subject, Integer> sp : allocatedSubjectPeriods.entrySet()) {
                Subject subj = sp.getKey();
                int periods = sp.getValue();
                if (periods <= 0) continue;

                Optional<TeachingAssignment> existingAsgOpt = teachingAssignmentRepository
                        .findBySchoolClassIdAndSubjectId(clazz.getId(), subj.getId());

                List<Employee> candidates = qualifiedTeachersBySubject.getOrDefault(subj.getId(), List.of());
                if (candidates.isEmpty()) {
                    throw new BusinessException("Môn học '" + subj.getName() + "' (" + subj.getCode()
                            + ") chưa có giáo viên nào đủ điều kiện chuyên môn đang làm việc (WORKING)");
                }

                Employee selectedTeacher = null;
                if (!autoAssignTeachers && existingAsgOpt.isPresent()
                        && existingAsgOpt.get().getStatus() == TeachingAssignmentStatus.ACTIVE
                        && existingAsgOpt.get().getTeacher() != null
                        && existingAsgOpt.get().getTeacher().getStatus() == EmployeeStatus.WORKING) {
                    selectedTeacher = existingAsgOpt.get().getTeacher();
                } else {
                    // Công thức chọn giáo viên tối ưu theo ĐM_thực (Thông tư 05/2025 & Thông tư 21/2025):
                    // Cost(t, c, s) = (W(t) + p) / ĐM_thực(t) + 0.06 * Classes(t) - 0.30 * I(t == Homeroom(c)) + OvertimeCapPenalty
                    double bestScore = Double.MAX_VALUE;
                    for (Employee candidate : candidates) {
                        int currentLoad = teacherRunningPeriods.getOrDefault(candidate.getId(), 0);
                        int currentClasses = teacherRunningClasses.getOrDefault(candidate.getId(), 0);
                        TeacherNormProfile profile = teacherNormProfiles.getOrDefault(
                                candidate.getId(), new TeacherNormProfile(DEFAULT_STANDARD_NORM_THCS, 0, "Không kiêm nhiệm", DEFAULT_STANDARD_NORM_THCS));
                        int effectiveNorm = Math.max(2, profile.effectiveNorm());

                        int projectedLoad = currentLoad + periods;
                        double loadRatio = (double) projectedLoad / effectiveNorm;
                        double classPenalty = 0.06 * currentClasses;
                        double homeroomBonus = (clazz.getHomeroomTeacher() != null
                                && clazz.getHomeroomTeacher().getId().equals(candidate.getId())) ? -0.30 : 0.0;

                        // Phạt nếu vượt trần thanh toán 200 tiết/năm (~5.71 tiết/tuần) hoặc vượt 50% ĐM chuẩn (9.5 tiết/tuần)
                        double projectedOvertimeWeekly = projectedLoad - effectiveNorm;
                        double overtimeCapPenalty = 0.0;
                        if (projectedOvertimeWeekly * WEEKS_PER_YEAR > MAX_PAID_OVERTIME_YEARLY) {
                            overtimeCapPenalty += 0.80;
                        }
                        if (projectedOvertimeWeekly > MAX_WEEKLY_OVERTIME_50_PCT) {
                            overtimeCapPenalty += 2.50;
                        }

                        double score = loadRatio + classPenalty + homeroomBonus + overtimeCapPenalty;
                        if (score < bestScore) {
                            bestScore = score;
                            selectedTeacher = candidate;
                        }
                    }
                }

                TeachingAssignment asg;
                if (existingAsgOpt.isPresent()) {
                    asg = existingAsgOpt.get();
                    asg.setTeacher(selectedTeacher);
                    asg.setStatus(TeachingAssignmentStatus.ACTIVE);
                    asg.setWeeklyContactHours((double) periods);
                    asg.setSubjectName(subj.getName() + " " + clazz.getGradeLevel());
                    asg.setGradeLevel("Khối " + clazz.getGradeLevel());
                    asg = teachingAssignmentRepository.save(asg);
                } else {
                    asg = teachingAssignmentRepository.save(TeachingAssignment.builder()
                            .schoolClass(clazz)
                            .subject(subj)
                            .teacher(selectedTeacher)
                            .status(TeachingAssignmentStatus.ACTIVE)
                            .subjectName(subj.getName() + " " + clazz.getGradeLevel())
                            .gradeLevel("Khối " + clazz.getGradeLevel())
                            .weeklyContactHours((double) periods)
                            .curriculum(Curriculum.MOET)
                            .build());
                }

                trackedTeachers.put(selectedTeacher.getId(), selectedTeacher);
                teacherRunningPeriods.merge(selectedTeacher.getId(), periods, Integer::sum);
                teacherRunningClasses.merge(selectedTeacher.getId(), 1, Integer::sum);
                assignmentMap.put(clazz.getId() + ":" + subj.getId(), asg);
                totalAssignmentsAllocated++;
            }
        }

        // 6. PHA 2: Chuẩn bị Phòng học & Thời khóa biểu cho tất cả các lớp
        List<Classroom> availableRooms = classroomRepository.findAll().stream()
                .filter(r -> r.getStatus() == ClassroomStatus.AVAILABLE)
                .sorted(Comparator.comparing(Classroom::getCapacity).thenComparing(Classroom::getCode))
                .collect(Collectors.toList());

        if (availableRooms.isEmpty()) {
            throw new BusinessException("Không có phòng học nào ở trạng thái AVAILABLE để xếp lịch");
        }

        // Gán phòng học chính (Home Classroom) cho từng lớp để hạn chế di chuyển
        Map<Long, Classroom> classHomeRoomMap = assignHomeClassrooms(activeClasses, availableRooms);

        // Xử lý Timetable hiện có của các lớp trong học kỳ
        Map<Long, Timetable> classTimetableMap = new HashMap<>();
        for (SchoolClass clazz : activeClasses) {
            List<Timetable> existingTimetables = timetableRepository
                    .findBySchoolClassIdAndSemesterIdOrderByVersionDesc(clazz.getId(), semester.getId());

            if (!existingTimetables.isEmpty() && overwriteExisting) {
                // Xóa các tiết học cũ trong học kỳ của lớp này để tái lập lịch đồng bộ toàn trường
                for (Timetable oldTt : existingTimetables) {
                    timetableEntryRepository.deleteByTimetableId(oldTt.getId());
                }
                Timetable latest = existingTimetables.get(0);
                // Chuyển các bản khác sang ARCHIVED
                for (int i = 1; i < existingTimetables.size(); i++) {
                    Timetable older = existingTimetables.get(i);
                    older.setStatus(TimetableStatus.ARCHIVED);
                    timetableRepository.save(older);
                }
                latest.setStatus(TimetableStatus.DRAFT);
                latest.setValidationNotes("Được tạo tự động bởi Auto-Scheduler");
                classTimetableMap.put(clazz.getId(), timetableRepository.save(latest));
            } else if (existingTimetables.isEmpty()) {
                Timetable created = timetableRepository.save(Timetable.builder()
                        .schoolClass(clazz)
                        .semester(semester)
                        .version(1)
                        .status(TimetableStatus.DRAFT)
                        .validationNotes("Được tạo tự động bởi Auto-Scheduler")
                        .build());
                classTimetableMap.put(clazz.getId(), created);
            } else {
                Timetable latest = existingTimetables.get(0);
                timetableEntryRepository.deleteByTimetableId(latest.getId());
                latest.setStatus(TimetableStatus.DRAFT);
                classTimetableMap.put(clazz.getId(), timetableRepository.save(latest));
            }
        }
        timetableEntryRepository.flush();

        // 7. PHA 2B: Thuật toán Xếp lịch Đa lớp (Multi-Class CSP + MRV Heuristic + Kempe Swap Repair)
        // Khởi tạo danh sách Task xếp lịch và sắp xếp theo độ khó giảm dần (MRV)
        List<SchedulingTask> tasks = new ArrayList<>();
        for (SchoolClass clazz : activeClasses) {
            for (Map.Entry<Subject, Integer> sp : allocatedSubjectPeriods.entrySet()) {
                Subject subj = sp.getKey();
                int periods = sp.getValue();
                if (periods <= 0) continue;

                TeachingAssignment asg = assignmentMap.get(clazz.getId() + ":" + subj.getId());
                int teacherTotalLoad = teacherRunningPeriods.getOrDefault(asg.getTeacher().getId(), 0);
                int specializedBonus = isSpecializedSubject(subj.getCode()) ? 25 : 0;
                int priority = periods * 12 + teacherTotalLoad + specializedBonus;

                tasks.add(new SchedulingTask(clazz, subj, asg, periods, priority));
            }
        }

        tasks.sort(Comparator.comparingInt(SchedulingTask::priorityScore).reversed());

        // Cấu trúc trạng thái toàn cục trong bộ nhớ
        // Key cho slot thời gian: day + ":" + slotId
        Map<Long, Map<String, PlannedCell>> classGrid = new HashMap<>();
        Map<Long, Set<String>> teacherBusySlots = new HashMap<>();
        Map<Long, Set<String>> roomBusySlots = new HashMap<>();
        Map<String, Integer> classSubjectDayCount = new HashMap<>(); // classId:subjectId:day -> count
        Map<String, Integer> classDayCount = new HashMap<>();        // classId:day -> count
        Map<String, Integer> teacherDayCount = new HashMap<>();      // teacherId:day -> count

        for (SchoolClass clazz : activeClasses) {
            classGrid.put(clazz.getId(), new HashMap<>());
        }

        int totalPeriodsScheduled = 0;
        int unplacedPeriods = 0;

        for (SchedulingTask task : tasks) {
            SchoolClass clazz = task.schoolClass();
            Subject subj = task.subject();
            TeachingAssignment asg = task.assignment();
            Employee teacher = asg.getTeacher();

            int dynamicMaxPerDay = Math.max(
                    maxPerSubjectPerDay,
                    (int) Math.ceil((double) task.periodsRequired() / activeDays.size()));

            for (int p = 0; p < task.periodsRequired(); p++) {
                PlannedCell placed = tryPlaceSinglePeriod(
                        clazz, subj, asg, teacher,
                        activeDays, candidateSlots, dynamicMaxPerDay,
                        availableRooms, classHomeRoomMap,
                        classGrid, teacherBusySlots, roomBusySlots,
                        classSubjectDayCount, classDayCount, teacherDayCount
                );

                if (placed == null) {
                    // Thử cơ chế Kempe Single-Step Swap Repair (Hoán đổi 1 bước để giải phóng xung đột)
                    placed = trySwapAndPlacePeriod(
                            clazz, subj, asg, teacher,
                            activeDays, candidateSlots, dynamicMaxPerDay,
                            availableRooms, classHomeRoomMap,
                            classGrid, teacherBusySlots, roomBusySlots,
                            classSubjectDayCount, classDayCount, teacherDayCount
                    );
                }

                if (placed == null) {
                    // Nới lỏng giới hạn số tiết/ngày của môn học nếu vẫn chưa đặt được
                    placed = tryPlaceSinglePeriod(
                            clazz, subj, asg, teacher,
                            activeDays, candidateSlots, candidateSlots.size(),
                            availableRooms, classHomeRoomMap,
                            classGrid, teacherBusySlots, roomBusySlots,
                            classSubjectDayCount, classDayCount, teacherDayCount
                    );
                }

                if (placed != null) {
                    totalPeriodsScheduled++;
                } else {
                    unplacedPeriods++;
                    log.warn("Không thể tìm thấy ô trống cho Lớp {} - Môn {} (GV: {})",
                            clazz.getName(), subj.getName(), teacher.getFullName());
                }
            }
        }

        // 8. Lưu toàn bộ TimetableEntry xuống cơ sở dữ liệu & Kiểm tra ràng buộc / Công bố
        List<ClassScheduleSummary> classSummaries = new ArrayList<>();

        for (SchoolClass clazz : activeClasses) {
            Timetable tt = classTimetableMap.get(clazz.getId());
            Map<String, PlannedCell> grid = classGrid.get(clazz.getId());

            List<TimetableEntry> entriesToSave = new ArrayList<>();
            for (PlannedCell cell : grid.values()) {
                entriesToSave.add(TimetableEntry.builder()
                        .timetable(tt)
                        .teachingAssignment(cell.assignment)
                        .dayOfWeek(cell.dayOfWeek)
                        .timeSlot(cell.timeSlot)
                        .classroom(cell.classroom)
                        .build());
            }
            timetableEntryRepository.saveAll(entriesToSave);
            timetableEntryRepository.flush();

            // Kiểm tra qua bộ TimetableValidator chuẩn của hệ thống
            ValidationResult validationResult = timetableValidator.validate(tt);
            List<String> notes = new ArrayList<>();

            if (validationResult.isValid()) {
                if (autoPublish) {
                    tt.setStatus(TimetableStatus.PUBLISHED);
                    tt.setPublishedAt(LocalDateTime.now());
                    tt.setValidationNotes("Tự động xếp lịch và công bố thành công (0 lỗi xung đột).");
                    notes.add("Đã kiểm tra hợp lệ 100% và công bố (PUBLISHED)");
                } else {
                    tt.setStatus(TimetableStatus.REVIEW);
                    tt.setValidationNotes("Tự động xếp lịch hợp lệ, đang chờ duyệt (REVIEW).");
                    notes.add("Hợp lệ 100% - Trạng thái: CHỜ DUYỆT (REVIEW)");
                }
            } else {
                tt.setStatus(TimetableStatus.DRAFT);
                String errSummary = validationResult.getErrors().stream()
                        .map(e -> e.getMessage())
                        .limit(3)
                        .collect(Collectors.joining("; "));
                tt.setValidationNotes("Cảnh báo kiểm tra: " + errSummary);
                validationResult.getErrors().forEach(e -> notes.add(e.getMessage()));
            }
            timetableRepository.save(tt);

            List<String> assignedTeacherLabels = allocatedSubjectPeriods.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(e -> {
                        TeachingAssignment a = assignmentMap.get(clazz.getId() + ":" + e.getKey().getId());
                        return e.getKey().getName() + ": " + (a != null ? a.getTeacher().getFullName() : "N/A")
                                + " (" + e.getValue() + " tiết)";
                    })
                    .collect(Collectors.toList());

            classSummaries.add(ClassScheduleSummary.builder()
                    .schoolClassId(clazz.getId())
                    .schoolClassName(clazz.getName())
                    .gradeLevel(clazz.getGradeLevel())
                    .timetableId(tt.getId())
                    .version(tt.getVersion())
                    .status(tt.getStatus().name())
                    .requiredPeriods(targetWeeklyPeriods)
                    .scheduledPeriods(entriesToSave.size())
                    .valid(validationResult.isValid())
                    .assignedTeachers(assignedTeacherLabels)
                    .notes(notes)
                    .build());
        }

        // 9. Tổng hợp báo cáo tải giảng dạy của Giáo viên theo Thông tư 05/2025 & Thông tư 21/2025
        List<TeacherLoadSummary> teacherLoadSummaries = trackedTeachers.values().stream()
                .filter(t -> teacherRunningPeriods.getOrDefault(t.getId(), 0) > 0)
                .map(t -> {
                    TeacherNormProfile profile = teacherNormProfiles.getOrDefault(
                            t.getId(), new TeacherNormProfile(DEFAULT_STANDARD_NORM_THCS, 0, "Không kiêm nhiệm", DEFAULT_STANDARD_NORM_THCS));
                    int assigned = teacherRunningPeriods.getOrDefault(t.getId(), 0);
                    int clsCount = teacherRunningClasses.getOrDefault(t.getId(), 0);
                    int effectiveNorm = Math.max(1, profile.effectiveNorm());
                    int weeklyDiff = assigned - effectiveNorm;
                    int yearlyDiff35Weeks = weeklyDiff * WEEKS_PER_YEAR;
                    double pct = Math.round(((double) assigned / effectiveNorm) * 1000.0) / 10.0;

                    String complianceStatus;
                    if (weeklyDiff > MAX_WEEKLY_OVERTIME_50_PCT) {
                        complianceStatus = "Vượt giới hạn 50% ĐM (> 9.5 tiết/tuần)";
                    } else if (yearlyDiff35Weeks > MAX_PAID_OVERTIME_YEARLY) {
                        complianceStatus = "Vượt trần 200 tiết/năm";
                    } else if (yearlyDiff35Weeks < 0) {
                        complianceStatus = "Thiếu định mức";
                    } else {
                        complianceStatus = "Hợp lệ";
                    }

                    return TeacherLoadSummary.builder()
                            .teacherId(t.getId())
                            .teacherCode(t.getEmployeeCode())
                            .teacherName(t.getFullName())
                            .departmentName(t.getDepartment() != null ? t.getDepartment().getName() : "")
                            .standardNormHours(profile.standardNorm())
                            .reductionHours(profile.reductionHours())
                            .reductionDetails(profile.reductionDetails())
                            .effectiveNormHours(effectiveNorm)
                            .assignedWeeklyPeriods(assigned)
                            .weeklyDifference(weeklyDiff)
                            .yearlyDifference35Weeks(yearlyDiff35Weeks)
                            .classesCount(clsCount)
                            .loadPercentage(pct)
                            .complianceStatus(complianceStatus)
                            .build();
                })
                .sorted(Comparator.comparing(TeacherLoadSummary::getDepartmentName)
                        .thenComparing(Comparator.comparingInt(TeacherLoadSummary::getAssignedWeeklyPeriods).reversed()))
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("=== AutoScheduleEngine: Đã xếp {} tiết cho {} lớp trong {} ms ===",
                totalPeriodsScheduled, activeClasses.size(), elapsed);

        return AutoScheduleResultResponse.builder()
                .semesterId(semester.getId())
                .semesterName(semester.getName())
                .academicYearName(academicYear.getName())
                .totalClassesScheduled(activeClasses.size())
                .totalAssignmentsAllocated(totalAssignmentsAllocated)
                .totalPeriodsScheduled(totalPeriodsScheduled)
                .unplacedPeriods(unplacedPeriods)
                .executionTimeMs(elapsed)
                .allocatedPeriodsPerSubject(allocatedByCode)
                .classResults(classSummaries)
                .teacherLoads(teacherLoadSummaries)
                .build();
    }

    /**
     * Phương pháp Phần dư lớn nhất (Hamilton / Largest Remainder Method):
     * Quy đổi tỉ lệ trọng số các môn học sang số tiết nguyên sao cho tổng số tiết bằng đúng targetWeeklyPeriods.
     */
    private Map<Subject, Integer> calculateHamiltonSubjectPeriods(
            List<Subject> subjects, Map<String, Double> ratios, int targetTotal) {

        Map<Subject, Double> activeWeights = new LinkedHashMap<>();
        double totalWeight = 0.0;

        for (Subject s : subjects) {
            Double w = ratios.get(s.getCode());
            if (w == null) {
                w = DEFAULT_SUBJECT_WEIGHTS.getOrDefault(s.getCode(), 0.0);
            }
            if (w != null && w > 0) {
                activeWeights.put(s, w);
                totalWeight += w;
            }
        }

        if (activeWeights.isEmpty() || totalWeight <= 0) {
            for (Subject s : subjects) {
                double w = DEFAULT_SUBJECT_WEIGHTS.getOrDefault(s.getCode(), 1.0);
                activeWeights.put(s, w);
                totalWeight += w;
            }
        }

        Map<Subject, Integer> result = new LinkedHashMap<>();
        Map<Subject, Double> remainders = new HashMap<>();
        int currentSum = 0;

        boolean canGuaranteeMinOne = targetTotal >= activeWeights.size();

        for (Map.Entry<Subject, Double> entry : activeWeights.entrySet()) {
            Subject s = entry.getKey();
            double exactQuota = (entry.getValue() / totalWeight) * targetTotal;
            int floorVal = (int) Math.floor(exactQuota);
            if (canGuaranteeMinOne && floorVal < 1) {
                floorVal = 1;
                remainders.put(s, -1.0); // Đã được ưu tiên nâng lên 1 tiết tối thiểu
            } else {
                remainders.put(s, exactQuota - floorVal);
            }
            result.put(s, floorVal);
            currentSum += floorVal;
        }

        int diff = targetTotal - currentSum;
        if (diff > 0) {
            List<Subject> sortedByRemainderDesc = new ArrayList<>(activeWeights.keySet());
            sortedByRemainderDesc.sort((a, b) -> Double.compare(remainders.getOrDefault(b, 0.0), remainders.getOrDefault(a, 0.0)));
            int idx = 0;
            while (diff > 0 && !sortedByRemainderDesc.isEmpty()) {
                Subject target = sortedByRemainderDesc.get(idx % sortedByRemainderDesc.size());
                result.put(target, result.get(target) + 1);
                diff--;
                idx++;
            }
        } else if (diff < 0) {
            List<Subject> sortedByPeriodsDesc = new ArrayList<>(activeWeights.keySet());
            sortedByPeriodsDesc.sort((a, b) -> Integer.compare(result.getOrDefault(b, 0), result.getOrDefault(a, 0)));
            int idx = 0;
            while (diff < 0 && !sortedByPeriodsDesc.isEmpty()) {
                Subject target = sortedByPeriodsDesc.get(idx % sortedByPeriodsDesc.size());
                int cur = result.getOrDefault(target, 0);
                if (cur > 1) {
                    result.put(target, cur - 1);
                    diff++;
                }
                idx++;
                if (idx > sortedByPeriodsDesc.size() * 10) break;
            }
        }

        for (Subject s : subjects) {
            result.putIfAbsent(s, 0);
        }

        return result;
    }

    /**
     * Tìm ô (Thứ, Tiết, Phòng học) có điểm phạt thấp nhất (Penalty Minimization) cho 1 tiết học.
     */
    private PlannedCell tryPlaceSinglePeriod(
            SchoolClass clazz, Subject subj, TeachingAssignment asg, Employee teacher,
            List<Integer> activeDays, List<TimeSlot> candidateSlots, int maxPerSubjectPerDay,
            List<Classroom> availableRooms, Map<Long, Classroom> classHomeRoomMap,
            Map<Long, Map<String, PlannedCell>> classGrid,
            Map<Long, Set<String>> teacherBusySlots,
            Map<Long, Set<String>> roomBusySlots,
            Map<String, Integer> classSubjectDayCount,
            Map<String, Integer> classDayCount,
            Map<String, Integer> teacherDayCount) {

        Map<String, PlannedCell> cGrid = classGrid.get(clazz.getId());
        Set<String> tBusy = teacherBusySlots.computeIfAbsent(teacher.getId(), k -> new HashSet<>());

        int bestPenalty = Integer.MAX_VALUE;
        Integer bestDay = null;
        TimeSlot bestSlot = null;
        Classroom bestRoom = null;

        for (Integer day : activeDays) {
            String csDayKey = clazz.getId() + ":" + subj.getId() + ":" + day;
            int subjOnDay = classSubjectDayCount.getOrDefault(csDayKey, 0);
            if (subjOnDay >= maxPerSubjectPerDay) {
                continue;
            }

            int cDayLoad = classDayCount.getOrDefault(clazz.getId() + ":" + day, 0);
            int tDayLoad = teacherDayCount.getOrDefault(teacher.getId() + ":" + day, 0);

            for (TimeSlot slot : candidateSlots) {
                String slotKey = day + ":" + slot.getId();

                // Ràng buộc cứng H1 & H2: Lớp chưa có tiết và Giáo viên không bị trùng giờ
                if (cGrid.containsKey(slotKey) || tBusy.contains(slotKey)) {
                    continue;
                }

                // Ràng buộc cứng H3: Tìm phòng học phù hợp sức chứa và chuyên môn còn trống
                Classroom room = findSuitableAvailableRoom(
                        clazz, subj, slotKey, availableRooms, classHomeRoomMap.get(clazz.getId()), roomBusySlots);
                if (room == null) {
                    continue;
                }

                // Kiểm tra có liền kề tiết cùng môn trong cùng buổi không (ưu tiên tiết đôi liền nhau nếu học 2 tiết/ngày)
                boolean isAdjacentSameSubject = isAdjacentToSameSubject(cGrid, subj.getId(), day, slot);

                // Công thức hàm mục tiêu Penalty (càng nhỏ càng tối ưu):
                // - Ưu tiên rải đều các ngày trong tuần (cDayLoad * 12)
                // - Ưu tiên rải đều môn học sang ngày khác (subjOnDay * 35), trừ khi ghép tiết đôi liền kề (-25)
                // - Ưu tiên lấp đầy từ tiết 1 trở đi (slot.periodNumber * 3) để tránh tiết trống giữa buổi
                int penalty = (subjOnDay * 35)
                        + (cDayLoad * 12)
                        + (tDayLoad * 8)
                        + (slot.getPeriodNumber() * 3)
                        - (isAdjacentSameSubject ? 25 : 0);

                if (penalty < bestPenalty) {
                    bestPenalty = penalty;
                    bestDay = day;
                    bestSlot = slot;
                    bestRoom = room;
                }
            }
        }

        if (bestDay != null && bestSlot != null && bestRoom != null) {
            PlannedCell cell = new PlannedCell();
            cell.schoolClass = clazz;
            cell.subject = subj;
            cell.assignment = asg;
            cell.teacher = teacher;
            cell.classroom = bestRoom;
            cell.dayOfWeek = bestDay;
            cell.timeSlot = bestSlot;

            commitCell(cell, classGrid, teacherBusySlots, roomBusySlots,
                    classSubjectDayCount, classDayCount, teacherDayCount);
            return cell;
        }

        return null;
    }

    /**
     * Kempe-Style Single-Step Swap Repair:
     * Khi một tiết học bị kẹt vì mọi ô trống của lớp đều trùng lịch giáo viên,
     * tìm một ô (day1, slot1) mà giáo viên mới đang RẢNH nhưng lớp đang học môn cũ (oldCell),
     * sau đó di chuyển oldCell sang một ô trống (day2, slot2) mà giáo viên của oldCell cũng RẢNH.
     */
    private PlannedCell trySwapAndPlacePeriod(
            SchoolClass clazz, Subject subj, TeachingAssignment asg, Employee teacher,
            List<Integer> activeDays, List<TimeSlot> candidateSlots, int maxPerSubjectPerDay,
            List<Classroom> availableRooms, Map<Long, Classroom> classHomeRoomMap,
            Map<Long, Map<String, PlannedCell>> classGrid,
            Map<Long, Set<String>> teacherBusySlots,
            Map<Long, Set<String>> roomBusySlots,
            Map<String, Integer> classSubjectDayCount,
            Map<String, Integer> classDayCount,
            Map<String, Integer> teacherDayCount) {

        Map<String, PlannedCell> cGrid = classGrid.get(clazz.getId());
        Set<String> newTeacherBusy = teacherBusySlots.computeIfAbsent(teacher.getId(), k -> new HashSet<>());

        for (Integer day1 : activeDays) {
            String csDayKey1 = clazz.getId() + ":" + subj.getId() + ":" + day1;
            if (classSubjectDayCount.getOrDefault(csDayKey1, 0) >= maxPerSubjectPerDay) {
                continue;
            }

            for (TimeSlot slot1 : candidateSlots) {
                String key1 = day1 + ":" + slot1.getId();
                // Nếu giáo viên mới bận ở key1 thì bỏ qua
                if (newTeacherBusy.contains(key1)) {
                    continue;
                }

                PlannedCell oldCell = cGrid.get(key1);
                if (oldCell == null || oldCell.subject.getId().equals(subj.getId())) {
                    continue;
                }

                // Tìm ô trống key2 cho oldCell
                Set<String> oldTeacherBusy = teacherBusySlots.computeIfAbsent(oldCell.teacher.getId(), k -> new HashSet<>());
                for (Integer day2 : activeDays) {
                    for (TimeSlot slot2 : candidateSlots) {
                        String key2 = day2 + ":" + slot2.getId();
                        if (cGrid.containsKey(key2) || oldTeacherBusy.contains(key2)) {
                            continue;
                        }

                        // Kiểm tra giới hạn môn/ngày nếu chuyển oldCell sang day2
                        if (!day1.equals(day2)) {
                            String oldCsDayKey2 = clazz.getId() + ":" + oldCell.subject.getId() + ":" + day2;
                            if (classSubjectDayCount.getOrDefault(oldCsDayKey2, 0) >= maxPerSubjectPerDay) {
                                continue;
                            }
                        }

                        Classroom roomForOldAtKey2 = findSuitableAvailableRoom(
                                clazz, oldCell.subject, key2, availableRooms,
                                classHomeRoomMap.get(clazz.getId()), roomBusySlots);
                        if (roomForOldAtKey2 == null) {
                            continue;
                        }

                        // Tạm gỡ oldCell khỏi key1
                        uncommitCell(oldCell, classGrid, teacherBusySlots, roomBusySlots,
                                classSubjectDayCount, classDayCount, teacherDayCount);

                        Classroom roomForNewAtKey1 = findSuitableAvailableRoom(
                                clazz, subj, key1, availableRooms,
                                classHomeRoomMap.get(clazz.getId()), roomBusySlots);

                        if (roomForNewAtKey1 != null) {
                            // Chuyển oldCell sang key2
                            oldCell.dayOfWeek = day2;
                            oldCell.timeSlot = slot2;
                            oldCell.classroom = roomForOldAtKey2;
                            commitCell(oldCell, classGrid, teacherBusySlots, roomBusySlots,
                                    classSubjectDayCount, classDayCount, teacherDayCount);

                            // Đặt tiết mới vào key1
                            PlannedCell newCell = new PlannedCell();
                            newCell.schoolClass = clazz;
                            newCell.subject = subj;
                            newCell.assignment = asg;
                            newCell.teacher = teacher;
                            newCell.classroom = roomForNewAtKey1;
                            newCell.dayOfWeek = day1;
                            newCell.timeSlot = slot1;
                            commitCell(newCell, classGrid, teacherBusySlots, roomBusySlots,
                                    classSubjectDayCount, classDayCount, teacherDayCount);
                            return newCell;
                        } else {
                            // Khôi phục lại oldCell nếu không lấy được phòng
                            commitCell(oldCell, classGrid, teacherBusySlots, roomBusySlots,
                                    classSubjectDayCount, classDayCount, teacherDayCount);
                        }
                    }
                }
            }
        }
        return null;
    }

    private void commitCell(
            PlannedCell cell,
            Map<Long, Map<String, PlannedCell>> classGrid,
            Map<Long, Set<String>> teacherBusySlots,
            Map<Long, Set<String>> roomBusySlots,
            Map<String, Integer> classSubjectDayCount,
            Map<String, Integer> classDayCount,
            Map<String, Integer> teacherDayCount) {

        String slotKey = cell.dayOfWeek + ":" + cell.timeSlot.getId();
        classGrid.get(cell.schoolClass.getId()).put(slotKey, cell);
        teacherBusySlots.computeIfAbsent(cell.teacher.getId(), k -> new HashSet<>()).add(slotKey);
        roomBusySlots.computeIfAbsent(cell.classroom.getId(), k -> new HashSet<>()).add(slotKey);

        classSubjectDayCount.merge(cell.schoolClass.getId() + ":" + cell.subject.getId() + ":" + cell.dayOfWeek, 1, Integer::sum);
        classDayCount.merge(cell.schoolClass.getId() + ":" + cell.dayOfWeek, 1, Integer::sum);
        teacherDayCount.merge(cell.teacher.getId() + ":" + cell.dayOfWeek, 1, Integer::sum);
    }

    private void uncommitCell(
            PlannedCell cell,
            Map<Long, Map<String, PlannedCell>> classGrid,
            Map<Long, Set<String>> teacherBusySlots,
            Map<Long, Set<String>> roomBusySlots,
            Map<String, Integer> classSubjectDayCount,
            Map<String, Integer> classDayCount,
            Map<String, Integer> teacherDayCount) {

        String slotKey = cell.dayOfWeek + ":" + cell.timeSlot.getId();
        classGrid.get(cell.schoolClass.getId()).remove(slotKey);
        teacherBusySlots.getOrDefault(cell.teacher.getId(), Set.of()).remove(slotKey);
        roomBusySlots.getOrDefault(cell.classroom.getId(), Set.of()).remove(slotKey);

        classSubjectDayCount.computeIfPresent(cell.schoolClass.getId() + ":" + cell.subject.getId() + ":" + cell.dayOfWeek, (k, v) -> v > 1 ? v - 1 : null);
        classDayCount.computeIfPresent(cell.schoolClass.getId() + ":" + cell.dayOfWeek, (k, v) -> v > 1 ? v - 1 : null);
        teacherDayCount.computeIfPresent(cell.teacher.getId() + ":" + cell.dayOfWeek, (k, v) -> v > 1 ? v - 1 : null);
    }

    private boolean isAdjacentToSameSubject(Map<String, PlannedCell> cGrid, Long subjectId, int day, TimeSlot candidateSlot) {
        for (PlannedCell existing : cGrid.values()) {
            if (existing.dayOfWeek == day
                    && existing.subject.getId().equals(subjectId)
                    && existing.timeSlot.getSession() == candidateSlot.getSession()
                    && Math.abs(existing.timeSlot.getPeriodNumber() - candidateSlot.getPeriodNumber()) == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecializedSubject(String code) {
        if (code == null) return false;
        String upper = code.toUpperCase();
        return upper.equals("IT") || upper.equals("PE") || upper.equals("KHTN")
                || upper.equals("PHYS") || upper.equals("CHEM") || upper.equals("BIO");
    }

    private Classroom findSuitableAvailableRoom(
            SchoolClass clazz, Subject subj, String slotKey,
            List<Classroom> availableRooms, Classroom homeRoom,
            Map<Long, Set<String>> roomBusySlots) {

        int requiredCap = clazz.getCapacity() != null ? clazz.getCapacity() : 0;
        String code = subj.getCode() != null ? subj.getCode().toUpperCase() : "";

        // 1. Ưu tiên phòng chức năng cho môn đặc thù (Tin học, Thể dục, KHTN)
        if ("PE".equals(code)) {
            for (Classroom r : availableRooms) {
                if (r.getRoomType() == ClassroomType.GYM
                        && r.getCapacity() >= requiredCap
                        && !isRoomBusy(r.getId(), slotKey, roomBusySlots)) {
                    return r;
                }
            }
        } else if ("IT".equals(code)) {
            for (Classroom r : availableRooms) {
                if (r.getRoomType() == ClassroomType.LAB
                        && r.getCode().toUpperCase().contains("IT")
                        && r.getCapacity() >= requiredCap
                        && !isRoomBusy(r.getId(), slotKey, roomBusySlots)) {
                    return r;
                }
            }
        } else if ("KHTN".equals(code) || "PHYS".equals(code) || "CHEM".equals(code) || "BIO".equals(code)) {
            for (Classroom r : availableRooms) {
                if (r.getRoomType() == ClassroomType.LAB
                        && !r.getCode().toUpperCase().contains("IT")
                        && r.getCapacity() >= requiredCap
                        && !isRoomBusy(r.getId(), slotKey, roomBusySlots)) {
                    return r;
                }
            }
        }

        // 2. Ưu tiên phòng học cố định (Home Room) của lớp nếu còn trống và đủ sức chứa
        if (homeRoom != null
                && homeRoom.getCapacity() >= requiredCap
                && !isRoomBusy(homeRoom.getId(), slotKey, roomBusySlots)) {
            return homeRoom;
        }

        // 3. Tìm phòng STANDARD bất kỳ đủ sức chứa và còn trống
        for (Classroom r : availableRooms) {
            if (r.getRoomType() == ClassroomType.STANDARD
                    && r.getCapacity() >= requiredCap
                    && !isRoomBusy(r.getId(), slotKey, roomBusySlots)) {
                return r;
            }
        }

        // 4. Fallback: Bất kỳ phòng học nào đủ sức chứa và còn trống
        for (Classroom r : availableRooms) {
            if (r.getCapacity() >= requiredCap && !isRoomBusy(r.getId(), slotKey, roomBusySlots)) {
                return r;
            }
        }

        return null;
    }

    private boolean isRoomBusy(Long roomId, String slotKey, Map<Long, Set<String>> roomBusySlots) {
        Set<String> busy = roomBusySlots.get(roomId);
        return busy != null && busy.contains(slotKey);
    }

    private Map<Long, Classroom> assignHomeClassrooms(List<SchoolClass> classes, List<Classroom> rooms) {
        Map<Long, Classroom> map = new HashMap<>();
        List<Classroom> standardRooms = rooms.stream()
                .filter(r -> r.getRoomType() == ClassroomType.STANDARD)
                .collect(Collectors.toList());

        Set<Long> usedRoomIds = new HashSet<>();
        for (SchoolClass clazz : classes) {
            int cap = clazz.getCapacity() != null ? clazz.getCapacity() : 0;
            for (Classroom r : standardRooms) {
                if (!usedRoomIds.contains(r.getId()) && r.getCapacity() >= cap) {
                    map.put(clazz.getId(), r);
                    usedRoomIds.add(r.getId());
                    break;
                }
            }
        }
        return map;
    }

    private Map<Long, TeacherNormProfile> loadTeacherNormProfiles(String academicYearName, List<SchoolClass> activeClasses) {
        Map<Long, Integer> positionNormMap = new HashMap<>();
        for (TeachingNorm norm : teachingNormRepository.findAll()) {
            if (norm.getPosition() != null && norm.getStandardHours() != null) {
                positionNormMap.put(norm.getPosition().getId(), norm.getStandardHours());
            }
        }

        // Map teacherId -> tên lớp chủ nhiệm
        Map<Long, String> homeroomClassByTeacherId = new HashMap<>();
        for (SchoolClass sc : activeClasses) {
            if (sc.getHomeroomTeacher() != null) {
                homeroomClassByTeacherId.put(sc.getHomeroomTeacher().getId(), sc.getName());
            }
        }

        Map<Long, TeacherNormProfile> profiles = new HashMap<>();
        for (Employee emp : employeeRepository.findAll()) {
            PositionCode posCode = (emp.getPosition() != null) ? emp.getPosition().getCode() : null;

            // Định mức tiết dạy chuẩn theo Thông tư 05/2025/TT-BGDĐT: Giáo viên THCS = 19 tiết/tuần
            int standardNorm = DEFAULT_STANDARD_NORM_THCS;
            if (posCode == PositionCode.PRINCIPAL) {
                standardNorm = 2;
            } else if (posCode == PositionCode.VICE_PRINCIPAL) {
                standardNorm = 4;
            } else if (emp.getPosition() != null && positionNormMap.containsKey(emp.getPosition().getId())) {
                int configured = positionNormMap.get(emp.getPosition().getId());
                if (configured >= 19) {
                    standardNorm = configured;
                }
            }

            // Tính giảm trừ kiêm nhiệm (tối đa 2 nhiệm vụ cao nhất: LARGE(F2:H2, 1) + LARGE(F2:H2, 2))
            List<Integer> reductions = new ArrayList<>();
            List<String> reductionLabels = new ArrayList<>();

            // 1. Kiêm nhiệm Giáo viên chủ nhiệm lớp: giảm 4 tiết/tuần
            if (homeroomClassByTeacherId.containsKey(emp.getId())) {
                reductions.add(4);
                reductionLabels.add("GVCN lớp " + homeroomClassByTeacherId.get(emp.getId()) + " (-4 tiết)");
            }

            // 2. Kiêm nhiệm Tổ trưởng chuyên môn / Trưởng bộ môn: giảm 3 tiết/tuần
            boolean isHod = (posCode == PositionCode.HEAD_OF_DEPARTMENT)
                    || (emp.getDepartment() != null
                        && emp.getDepartment().getManager() != null
                        && emp.getDepartment().getManager().getId().equals(emp.getId()));
            if (isHod) {
                reductions.add(3);
                reductionLabels.add("Tổ trưởng CM (-3 tiết)");
            }

            reductions.sort(Comparator.reverseOrder());
            int totalReduction = reductions.stream().limit(2).mapToInt(Integer::intValue).sum();
            int effectiveNorm = Math.max(2, standardNorm - totalReduction);
            String details = reductionLabels.isEmpty() ? "Không kiêm nhiệm" : String.join(", ", reductionLabels);

            profiles.put(emp.getId(), new TeacherNormProfile(standardNorm, totalReduction, details, effectiveNorm));
        }
        return profiles;
    }
}
