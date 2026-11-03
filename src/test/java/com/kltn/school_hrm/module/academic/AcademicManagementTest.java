package com.kltn.school_hrm.module.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kltn.school_hrm.module.academic.dto.request.AcademicYearRequest;
import com.kltn.school_hrm.module.academic.dto.request.ClassSubjectRequest;
import com.kltn.school_hrm.module.academic.dto.request.SchoolClassRequest;
import com.kltn.school_hrm.module.academic.dto.request.SemesterRequest;
import com.kltn.school_hrm.module.academic.dto.response.AcademicYearResponse;
import com.kltn.school_hrm.module.academic.dto.response.ClassSubjectResponse;
import com.kltn.school_hrm.module.academic.dto.response.SemesterResponse;
import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.AcademicYearRepository;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
import com.kltn.school_hrm.module.academic.service.implement.AcademicYearServiceImpl;
import com.kltn.school_hrm.module.academic.service.implement.ClassSubjectServiceImpl;
import com.kltn.school_hrm.module.academic.service.implement.SemesterServiceImpl;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

@ExtendWith(MockitoExtension.class)
public class AcademicManagementTest {

    @Mock
    private AcademicYearRepository academicYearRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private ClassSubjectRepository classSubjectRepository;

    private AcademicYearServiceImpl academicYearService;
    private SemesterServiceImpl semesterService;
    private ClassSubjectServiceImpl classSubjectService;

    private AcademicYear mockYear;

    @BeforeEach
    void setUp() {
        academicYearService = new AcademicYearServiceImpl(academicYearRepository);
        semesterService = new SemesterServiceImpl(semesterRepository, academicYearRepository);
        classSubjectService = new ClassSubjectServiceImpl(
                classSubjectRepository, schoolClassRepository, subjectRepository, semesterRepository);

        mockYear = AcademicYear.builder()
                .id(1L)
                .name("2026-2027")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 5, 31))
                .status(AcademicStatus.ACTIVE)
                .build();
    }

    // ==================== Academic Year Tests ====================

    @Test
    @DisplayName("BR-01: Năm học có ngày kết thúc trước ngày bắt đầu phải ném BusinessException")
    void shouldThrowExceptionWhenAcademicYearEndDateBeforeStartDate() {
        AcademicYearRequest request = AcademicYearRequest.builder()
                .name("2026-2027")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .status(AcademicStatus.ACTIVE)
                .build();

        assertThrows(BusinessException.class, () -> academicYearService.create(request));
    }

    @Test
    @DisplayName("Tạo năm học hợp lệ thành công")
    void shouldCreateAcademicYearSuccessfully() {
        AcademicYearRequest request = AcademicYearRequest.builder()
                .name("2026-2027")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 5, 31))
                .status(AcademicStatus.ACTIVE)
                .build();

        when(academicYearRepository.existsByName("2026-2027")).thenReturn(false);
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(mockYear);

        AcademicYearResponse response = academicYearService.create(request);
        assertNotNull(response);
        assertEquals("2026-2027", response.getName());
    }

    // ==================== Semester Tests ====================

    @Test
    @DisplayName("BR-02: Học kỳ nằm ngoài khoảng thời gian năm học phải ném BusinessException")
    void shouldThrowExceptionWhenSemesterOutsideAcademicYear() {
        SemesterRequest request = SemesterRequest.builder()
                .academicYearId(1L)
                .name("Học kỳ 1")
                .startDate(LocalDate.of(2026, 8, 15)) // Trước ngày 1/9/2026
                .endDate(LocalDate.of(2027, 1, 15))
                .status(AcademicStatus.ACTIVE)
                .build();

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(mockYear));

        assertThrows(BusinessException.class, () -> semesterService.create(request));
    }

    @Test
    @DisplayName("BR-03: Học kỳ bị chồng lấn thời gian với học kỳ đã có phải ném BusinessException")
    void shouldThrowExceptionWhenSemestersOverlap() {
        Semester existingSemester = Semester.builder()
                .id(10L)
                .academicYear(mockYear)
                .name("Học kỳ 1")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(AcademicStatus.COMPLETED)
                .build();

        SemesterRequest overlappingRequest = SemesterRequest.builder()
                .academicYearId(1L)
                .name("Học kỳ 2")
                .startDate(LocalDate.of(2027, 1, 10)) // Bắt đầu khi HK1 chưa kết thúc
                .endDate(LocalDate.of(2027, 5, 25))
                .status(AcademicStatus.PLANNING)
                .build();

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(mockYear));
        when(semesterRepository.findByAcademicYearId(1L)).thenReturn(List.of(existingSemester));

        assertThrows(BusinessException.class, () -> semesterService.create(overlappingRequest));
    }

    @Test
    @DisplayName("Tạo học kỳ hợp lệ nằm trong năm học và không chồng lấn thành công")
    void shouldCreateSemesterSuccessfully() {
        SemesterRequest request = SemesterRequest.builder()
                .academicYearId(1L)
                .name("Học kỳ 1")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(AcademicStatus.ACTIVE)
                .build();

        Semester created = Semester.builder()
                .id(10L)
                .academicYear(mockYear)
                .name("Học kỳ 1")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2027, 1, 15))
                .status(AcademicStatus.ACTIVE)
                .build();

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(mockYear));
        when(semesterRepository.findByAcademicYearId(1L)).thenReturn(List.of());
        when(semesterRepository.save(any(Semester.class))).thenReturn(created);

        SemesterResponse response = semesterService.create(request);
        assertNotNull(response);
        assertEquals("Học kỳ 1", response.getName());
    }

    // ==================== ClassSubject Tests ====================

    @Test
    @DisplayName("BR-05: Số tiết mỗi tuần <= 0 phải ném BusinessException")
    void shouldThrowExceptionWhenPeriodsPerWeekZeroOrNegative() {
        ClassSubjectRequest request = ClassSubjectRequest.builder()
                .schoolClassId(1L)
                .subjectId(2L)
                .semesterId(3L)
                .periodsPerWeek(0)
                .build();

        assertThrows(BusinessException.class, () -> classSubjectService.create(request));
    }

    @Test
    @DisplayName("BR-06: Một lớp không được khai báo trùng môn trong cùng một học kỳ")
    void shouldThrowExceptionWhenDuplicateSubjectInClassAndSemester() {
        ClassSubjectRequest request = ClassSubjectRequest.builder()
                .schoolClassId(1L)
                .subjectId(2L)
                .semesterId(3L)
                .periodsPerWeek(5)
                .build();

        SchoolClass mockClass = SchoolClass.builder().id(1L).name("10A1").build();
        Subject mockSubject = Subject.builder().id(2L).name("Toán học").code("MATH_10").build();
        Semester mockSemester = Semester.builder().id(3L).name("Học kỳ 1").build();

        when(schoolClassRepository.findById(1L)).thenReturn(Optional.of(mockClass));
        when(subjectRepository.findById(2L)).thenReturn(Optional.of(mockSubject));
        when(semesterRepository.findById(3L)).thenReturn(Optional.of(mockSemester));
        when(classSubjectRepository.existsBySchoolClassIdAndSubjectIdAndSemesterId(1L, 2L, 3L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> classSubjectService.create(request));
    }

    @Test
    @DisplayName("Tạo phân phối môn học hợp lệ thành công")
    void shouldCreateClassSubjectSuccessfully() {
        ClassSubjectRequest request = ClassSubjectRequest.builder()
                .schoolClassId(1L)
                .subjectId(2L)
                .semesterId(3L)
                .periodsPerWeek(5)
                .build();

        SchoolClass mockClass = SchoolClass.builder().id(1L).name("10A1").build();
        Subject mockSubject = Subject.builder().id(2L).name("Toán học").code("MATH_10").build();
        Semester mockSemester = Semester.builder().id(3L).name("Học kỳ 1").build();

        ClassSubject entity = ClassSubject.builder()
                .id(100L)
                .schoolClass(mockClass)
                .subject(mockSubject)
                .semester(mockSemester)
                .periodsPerWeek(5)
                .build();

        when(schoolClassRepository.findById(1L)).thenReturn(Optional.of(mockClass));
        when(subjectRepository.findById(2L)).thenReturn(Optional.of(mockSubject));
        when(semesterRepository.findById(3L)).thenReturn(Optional.of(mockSemester));
        when(classSubjectRepository.existsBySchoolClassIdAndSubjectIdAndSemesterId(1L, 2L, 3L)).thenReturn(false);
        when(classSubjectRepository.save(any(ClassSubject.class))).thenReturn(entity);

        ClassSubjectResponse response = classSubjectService.create(request);
        assertNotNull(response);
        assertEquals(5, response.getPeriodsPerWeek());
        assertEquals("10A1", response.getSchoolClassName());
        assertEquals("Toán học", response.getSubjectName());
    }
}

