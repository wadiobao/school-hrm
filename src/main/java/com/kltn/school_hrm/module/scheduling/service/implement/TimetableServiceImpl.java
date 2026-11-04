package com.kltn.school_hrm.module.scheduling.service.implement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.scheduling.constraint.TimetableValidator;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationResult;
import com.kltn.school_hrm.module.scheduling.dto.request.TimetableRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimeSlotResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableEntryResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableMatrixResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableResponse;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableRepository;
import com.kltn.school_hrm.module.scheduling.service.TimeSlotService;
import com.kltn.school_hrm.module.scheduling.service.TimetableEntryService;
import com.kltn.school_hrm.module.scheduling.service.TimetableService;
import com.kltn.school_hrm.module.scheduling.statemachine.TimetableStateMachine;
import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

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
}

