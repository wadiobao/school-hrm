package com.kltn.school_hrm.module.scheduling.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByTimetableId(Long timetableId);

    List<TimetableEntry> findByTimetableIdOrderByDayOfWeekAscTimeSlotPeriodNumberAsc(Long timetableId);

    void deleteByTimetableId(Long timetableId);

    // Đếm số tiết đã xếp trong một thời khóa biểu cho một phân công giảng dạy
    @Query("SELECT COUNT(te) FROM TimetableEntry te WHERE te.timetable.id = :timetableId AND te.teachingAssignment.id = :assignmentId")
    long countByTimetableIdAndAssignmentId(@Param("timetableId") Long timetableId, @Param("assignmentId") Long assignmentId);

    // Kiểm tra trùng lớp (BR-14): Một lớp không được có 2 tiết học cùng lúc trong cùng thời khóa biểu
    @Query("SELECT COUNT(te) FROM TimetableEntry te " +
           "WHERE te.timetable.id = :timetableId " +
           "AND te.dayOfWeek = :dayOfWeek " +
           "AND te.timeSlot.id = :timeSlotId " +
           "AND (:excludeEntryId IS NULL OR te.id <> :excludeEntryId)")
    long countClassConflicts(
            @Param("timetableId") Long timetableId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("timeSlotId") Long timeSlotId,
            @Param("excludeEntryId") Long excludeEntryId);

    // Kiểm tra trùng giáo viên (BR-15): Cả giáo viên chính và giáo viên thay thế trong học kỳ
    @Query("SELECT COUNT(te) FROM TimetableEntry te " +
           "JOIN te.timetable t " +
           "JOIN te.teachingAssignment ta " +
           "WHERE t.semester.id = :semesterId " +
           "AND t.status IN ('PUBLISHED', 'REVIEW') " +
           "AND t.id <> :currentTimetableId " +
           "AND te.dayOfWeek = :dayOfWeek " +
           "AND te.timeSlot.id = :timeSlotId " +
           "AND (ta.teacher.id = :teacherId OR (te.substituteTeacher IS NOT NULL AND te.substituteTeacher.id = :teacherId))")
    long countTeacherConflicts(
            @Param("semesterId") Long semesterId,
            @Param("currentTimetableId") Long currentTimetableId,
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("timeSlotId") Long timeSlotId);

    // Kiểm tra trùng phòng học (BR-16)
    @Query("SELECT COUNT(te) FROM TimetableEntry te " +
           "JOIN te.timetable t " +
           "WHERE t.semester.id = :semesterId " +
           "AND t.status IN ('PUBLISHED', 'REVIEW') " +
           "AND t.id <> :currentTimetableId " +
           "AND te.classroom.id = :classroomId " +
           "AND te.dayOfWeek = :dayOfWeek " +
           "AND te.timeSlot.id = :timeSlotId")
    long countClassroomConflicts(
            @Param("semesterId") Long semesterId,
            @Param("currentTimetableId") Long currentTimetableId,
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("timeSlotId") Long timeSlotId);

    // Kiểm tra xem tiết học có đang được sử dụng trong thời khóa biểu đã công bố không (SCH-06)
    @Query("SELECT COUNT(te) FROM TimetableEntry te JOIN te.timetable t WHERE te.timeSlot.id = :timeSlotId AND t.status = 'PUBLISHED'")
    long countEntriesByTimeSlotIdInPublishedTimetable(@Param("timeSlotId") Long timeSlotId);
}

