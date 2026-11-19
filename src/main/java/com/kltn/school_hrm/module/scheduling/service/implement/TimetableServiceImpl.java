package com.kltn.school_hrm.module.scheduling.service.implement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.scheduling.constraint.TimetableValidator;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationResult;
import com.kltn.school_hrm.module.scheduling.dto.request.TimetableRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TeacherPeriodItem;
import com.kltn.school_hrm.module.scheduling.dto.response.TeacherScheduleRow;
import com.kltn.school_hrm.module.scheduling.dto.response.TeacherTimetableResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimeSlotResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableEntryResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableMatrixResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableResponse;
import com.kltn.school_hrm.module.scheduling.entity.TimeSlot;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableRepository;
import com.kltn.school_hrm.module.scheduling.service.TimeSlotService;
import com.kltn.school_hrm.module.scheduling.service.TimetableEntryService;
import com.kltn.school_hrm.module.scheduling.service.TimetableService;
import com.kltn.school_hrm.module.scheduling.statemachine.TimetableStateMachine;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;
import com.kltn.school_hrm.shared.enums.Enums.RoleCode;
import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.shared.utils.SchedulingSecurity;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý thời khóa biểu, điều phối State Machine và công bố phiên bản.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository entryRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SemesterRepository semesterRepository;
    private final EmployeeRepository employeeRepository;
    private final SchedulingSecurity schedulingSecurity;
    private final TimeSlotService timeSlotService;
    private final TimetableEntryService entryService;
    private final TimetableValidator timetableValidator;
    private final TimetableStateMachine stateMachine;

    @Override
    public TimetableResponse create(TimetableRequest request) {
        SchoolClass schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + request.getSchoolClassId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + request.getSemesterId()));

        Integer maxVersion = timetableRepository.findMaxVersionByClassAndSemester(
                request.getSchoolClassId(), request.getSemesterId());
        int newVersion = (maxVersion != null ? maxVersion : 0) + 1;

        Timetable entity = Timetable.builder()
                .schoolClass(schoolClass)
                .semester(semester)
                .version(newVersion)
                .status(TimetableStatus.DRAFT)
                .build();

        return mapToResponse(timetableRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableResponse getById(Long id) {
        return timetableRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> getByClassAndSemester(Long schoolClassId, Long semesterId) {
        return timetableRepository.findBySchoolClassIdAndSemesterIdOrderByVersionDesc(schoolClassId, semesterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableResponse getPublishedTimetable(Long schoolClassId, Long semesterId) {
        return timetableRepository.findBySchoolClassIdAndSemesterIdAndStatus(
                schoolClassId, semesterId, TimetableStatus.PUBLISHED)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có thời khóa biểu được công bố cho lớp này trong học kỳ"));
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableMatrixResponse getMatrix(Long timetableId) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu với id: " + timetableId));

        List<TimeSlotResponse> allSlots = timeSlotService.getAll();
        List<TimetableEntryResponse> entries = entryService.getByTimetableId(timetableId);

        Map<String, TimetableEntryResponse> gridMap = new HashMap<>();
        for (TimetableEntryResponse entry : entries) {
            if (entry.getDayOfWeek() != null && entry.getTimeSlotId() != null) {
                String key = entry.getDayOfWeek() + ":" + entry.getTimeSlotId();
                gridMap.put(key, entry);
            }
        }

        return TimetableMatrixResponse.builder()
                .timetableId(timetable.getId())
                .schoolClassId(timetable.getSchoolClass().getId())
                .schoolClassName(timetable.getSchoolClass().getName())
                .semesterId(timetable.getSemester().getId())
                .semesterName(timetable.getSemester().getName())
                .version(timetable.getVersion())
                .status(timetable.getStatus())
                .timeSlots(allSlots)
                .gridEntries(gridMap)
                .build();
    }

    @Override
    public ValidationResult validateTimetable(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu với id: " + id));

        // Chuyển sang VALIDATING
        stateMachine.validateTransition(timetable.getStatus(), TimetableStatus.VALIDATING);
        timetable.setStatus(TimetableStatus.VALIDATING);

        ValidationResult result = timetableValidator.validate(timetable);

        if (result.isValid()) {
            // Validation thành công -> Chuyển sang REVIEW
            stateMachine.validateTransition(timetable.getStatus(), TimetableStatus.REVIEW);
            timetable.setStatus(TimetableStatus.REVIEW);
            timetable.setValidationNotes("Đã vượt qua toàn bộ các quy tắc kiểm tra hợp lệ.");
        } else {
            // Validation thất bại -> Quay lại DRAFT và lưu log lỗi
            stateMachine.validateTransition(timetable.getStatus(), TimetableStatus.DRAFT);
            timetable.setStatus(TimetableStatus.DRAFT);

            String errorSummary = result.getErrors().stream()
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining("; "));
            timetable.setValidationNotes("Lỗi kiểm tra: " + errorSummary);
        }

        timetableRepository.save(timetable);
        return result;
    }

    @Override
    public TimetableResponse publish(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu với id: " + id));

        // Phải ở trạng thái REVIEW mới được PUBLISHED
        if (timetable.getStatus() == TimetableStatus.DRAFT) {
            // Nếu đang DRAFT thì tự động kích hoạt validate trước
            ValidationResult vResult = validateTimetable(id);
            if (!vResult.isValid()) {
                throw new BusinessException("Thời khóa biểu chưa vượt qua kiểm tra ràng buộc, không thể công bố.");
            }
            // Nạp lại trạng thái mới nhất
            timetable = timetableRepository.findById(id).orElseThrow();
        }

        stateMachine.validateTransition(timetable.getStatus(), TimetableStatus.PUBLISHED);

        // Lưu trữ (ARCHIVED) bản đang PUBLISHED trước đó của lớp này trong học kỳ
        Optional<Timetable> currentPublished = timetableRepository.findBySchoolClassIdAndSemesterIdAndStatus(
                timetable.getSchoolClass().getId(), timetable.getSemester().getId(), TimetableStatus.PUBLISHED);
        currentPublished.ifPresent(prev -> {
            if (!prev.getId().equals(id)) {
                prev.setStatus(TimetableStatus.ARCHIVED);
                timetableRepository.save(prev);
            }
        });

        timetable.setStatus(TimetableStatus.PUBLISHED);
        timetable.setPublishedAt(LocalDateTime.now());

        return mapToResponse(timetableRepository.save(timetable));
    }

    @Override
    public TimetableResponse createNewVersionFromExisting(Long timetableId) {
        Timetable source = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu nguồn với id: " + timetableId));

        Integer maxVersion = timetableRepository.findMaxVersionByClassAndSemester(
                source.getSchoolClass().getId(), source.getSemester().getId());
        int newVersion = (maxVersion != null ? maxVersion : 0) + 1;

        Timetable newTimetable = Timetable.builder()
                .schoolClass(source.getSchoolClass())
                .semester(source.getSemester())
                .version(newVersion)
                .status(TimetableStatus.DRAFT)
                .validationNotes("Được sao chép từ phiên bản " + source.getVersion())
                .build();

        Timetable saved = timetableRepository.save(newTimetable);

        // Sao chép toàn bộ các tiết học từ phiên bản cũ sang phiên bản mới
        List<TimetableEntry> sourceEntries = entryRepository.findByTimetableId(source.getId());
        for (TimetableEntry se : sourceEntries) {
            TimetableEntry clone = TimetableEntry.builder()
                    .timetable(saved)
                    .teachingAssignment(se.getTeachingAssignment())
                    .substituteTeacher(se.getSubstituteTeacher())
                    .dayOfWeek(se.getDayOfWeek())
                    .timeSlot(se.getTimeSlot())
                    .classroom(se.getClassroom())
                    .build();
            entryRepository.save(clone);
        }

        return mapToResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu với id: " + id));

        if (timetable.getStatus() == TimetableStatus.PUBLISHED) {
            throw new BusinessException("Không thể xóa thời khóa biểu đã công bố (PUBLISHED)");
        }

        entryRepository.deleteByTimetableId(id);
        timetableRepository.deleteById(id);
    }

    private TimetableResponse mapToResponse(Timetable entity) {
        return TimetableResponse.builder()
                .id(entity.getId())
                .schoolClassId(entity.getSchoolClass() != null ? entity.getSchoolClass().getId() : null)
                .schoolClassName(entity.getSchoolClass() != null ? entity.getSchoolClass().getName() : null)
                .semesterId(entity.getSemester() != null ? entity.getSemester().getId() : null)
                .semesterName(entity.getSemester() != null ? entity.getSemester().getName() : null)
                .version(entity.getVersion())
                .status(entity.getStatus())
                .createdByUserId(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getId() : null)
                .createdByUserName(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getUsername() : null)
                .publishedAt(entity.getPublishedAt())
                .validationNotes(entity.getValidationNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherTimetableResponse getTeacherTimetable(Long semesterId, Long teacherId, Long departmentId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + semesterId));

        boolean canViewAll = schedulingSecurity.canViewAllTeacherSchedules();
        Optional<Employee> currentEmpOpt = schedulingSecurity.getCurrentEmployee();

        Long currentEmployeeId = currentEmpOpt.map(Employee::getId).orElse(null);
        String currentEmployeeName = currentEmpOpt.map(Employee::getFullName).orElse("N/A");

        // 1. Xác định danh sách giáo viên cần hiển thị theo phân quyền
        List<Employee> targetTeachers;
        if (canViewAll) {
            if (teacherId != null) {
                targetTeachers = employeeRepository.findById(teacherId).map(List::of).orElse(List.of());
            } else if (departmentId != null) {
                targetTeachers = employeeRepository.findAll().stream()
                        .filter(e -> e.getDepartment() != null && e.getDepartment().getId().equals(departmentId))
                        .filter(this::isTeacherOrHasAssignments)
                        .collect(Collectors.toList());
            } else {
                targetTeachers = employeeRepository.findAll().stream()
                        .filter(this::isTeacherOrHasAssignments)
                        .collect(Collectors.toList());
            }
        } else {
            // Cá nhân thông thường: CHỈ HIỂN THỊ CÁ NHÂN
            if (currentEmpOpt.isEmpty()) {
                throw new BusinessException("Tài khoản hiện tại chưa được liên kết với hồ sơ nhân viên/giáo viên");
            }
            targetTeachers = List.of(currentEmpOpt.get());
        }

        // 2. Lấy danh sách tiết học trong học kỳ này
        List<TimetableEntry> entries = entryRepository.findEntriesBySemesterAndStatus(semesterId, TimetableStatus.PUBLISHED);
        if (entries.isEmpty()) {
            entries = entryRepository.findEntriesBySemesterAndStatus(semesterId, null);
        }

        // 3. Gom nhóm tiết học theo Giáo viên và theo Thứ trong tuần
        Map<Long, Map<Integer, List<TeacherPeriodItem>>> teacherScheduleMap = new HashMap<>();

        for (TimetableEntry entry : entries) {
            TeachingAssignment ta = entry.getTeachingAssignment();
            if (ta == null) continue;

            Employee mainTeacher = ta.getTeacher();
            Employee subTeacher = entry.getSubstituteTeacher();
            Integer day = entry.getDayOfWeek();
            if (day == null) continue;

            // Nếu có giáo viên dạy thay:
            if (subTeacher != null) {
                TeacherPeriodItem subItem = mapToTeacherPeriodItem(entry, ta, true, "Dạy thay cho " + (mainTeacher != null ? mainTeacher.getFullName() : ""));
                teacherScheduleMap.computeIfAbsent(subTeacher.getId(), k -> new HashMap<>())
                        .computeIfAbsent(day, k -> new ArrayList<>())
                        .add(subItem);
            }

            if (mainTeacher != null) {
                if (subTeacher == null) {
                    TeacherPeriodItem mainItem = mapToTeacherPeriodItem(entry, ta, false, null);
                    teacherScheduleMap.computeIfAbsent(mainTeacher.getId(), k -> new HashMap<>())
                            .computeIfAbsent(day, k -> new ArrayList<>())
                            .add(mainItem);
                } else {
                    TeacherPeriodItem noteItem = mapToTeacherPeriodItem(entry, ta, false, "Có " + subTeacher.getFullName() + " dạy thay");
                    teacherScheduleMap.computeIfAbsent(mainTeacher.getId(), k -> new HashMap<>())
                            .computeIfAbsent(day, k -> new ArrayList<>())
                            .add(noteItem);
                }
            }
        }

        // 4. Xây dựng danh sách TeacherScheduleRow
        List<TeacherScheduleRow> rows = new ArrayList<>();
        for (Employee teacher : targetTeachers) {
            Map<Integer, List<TeacherPeriodItem>> dayMap = teacherScheduleMap.getOrDefault(teacher.getId(), new HashMap<>());

            int teacherTotalPeriods = 0;
            Map<Integer, List<TeacherPeriodItem>> sortedDayMap = new HashMap<>();
            for (int d = 1; d <= 7; d++) {
                List<TeacherPeriodItem> items = dayMap.getOrDefault(d, new ArrayList<>());
                items.sort(Comparator.comparing(TeacherPeriodItem::getPeriodNumber, Comparator.nullsLast(Comparator.naturalOrder())));
                sortedDayMap.put(d, items);
                teacherTotalPeriods += items.size();
            }

            TeacherScheduleRow row = TeacherScheduleRow.builder()
                    .teacherId(teacher.getId())
                    .teacherCode(teacher.getEmployeeCode())
                    .teacherName(teacher.getFullName())
                    .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : "")
                    .positionName(teacher.getPosition() != null ? teacher.getPosition().getName() : "")
                    .totalPeriods(teacherTotalPeriods)
                    .scheduleByDay(sortedDayMap)
                    .build();

            rows.add(row);
        }

        rows.sort(Comparator.comparing(TeacherScheduleRow::getTeacherName, Comparator.nullsLast(Comparator.naturalOrder())));

        String academicYearName = semester.getAcademicYear() != null ? semester.getAcademicYear().getName() : "";

        return TeacherTimetableResponse.builder()
                .semesterId(semester.getId())
                .semesterName(semester.getName())
                .academicYearName(academicYearName)
                .canViewAll(canViewAll)
                .currentEmployeeId(currentEmployeeId)
                .currentEmployeeName(currentEmployeeName)
                .rows(rows)
                .build();
    }

    private boolean isTeacherOrHasAssignments(Employee emp) {
        if (emp.getPosition() != null && emp.getPosition().getCode() == PositionCode.TEACHER) {
            return true;
        }
        if (emp.getUser() != null && emp.getUser().getRole() != null && emp.getUser().getRole().getRoleCode() == RoleCode.TEACHER) {
            return true;
        }
        return true;
    }

    private TeacherPeriodItem mapToTeacherPeriodItem(TimetableEntry entry, TeachingAssignment ta, boolean isSubstitute, String note) {
        TimeSlot slot = entry.getTimeSlot();
        Classroom room = entry.getClassroom();
        SchoolClass sc = ta.getSchoolClass();
        Subject subject = ta.getSubject();

        String slotRange = slot != null ? (slot.getStartTime() + " - " + slot.getEndTime()) : "";

        return TeacherPeriodItem.builder()
                .entryId(entry.getId())
                .periodNumber(slot != null ? slot.getPeriodNumber() : null)
                .timeSlotRange(slotRange)
                .session(slot != null ? slot.getSession() : null)
                .schoolClassId(sc != null ? sc.getId() : null)
                .schoolClassName(sc != null ? sc.getName() : null)
                .subjectId(subject != null ? subject.getId() : null)
                .subjectCode(subject != null ? subject.getCode() : null)
                .subjectName(ta.getSubjectName())
                .classroomId(room != null ? room.getId() : null)
                .classroomCode(room != null ? room.getCode() : null)
                .classroomName(room != null ? room.getName() : null)
                .isSubstitute(isSubstitute)
                .substituteNote(note)
                .build();
    }
}

