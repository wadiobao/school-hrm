package com.kltn.school_hrm.module.scheduling.entity;

import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.shared.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity lưu một tiết học cụ thể trên lưới thời khóa biểu.
 * Gắn liền 5 yếu tố: Lớp, Môn, Giáo viên chính, Giáo viên thay thế (nếu có), Phòng học, và Khung giờ.
 */
@Entity
@Table(name = "timetable_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TimetableEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_teacher_id")
    private Employee substituteTeacher;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1 = MONDAY, 2 = TUESDAY, ..., 7 = SUNDAY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
}

