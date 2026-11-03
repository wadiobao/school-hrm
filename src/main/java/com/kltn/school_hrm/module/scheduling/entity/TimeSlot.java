package com.kltn.school_hrm.module.scheduling.entity;

import java.time.LocalTime;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity quản lý khung giờ / tiết học trong ngày.
 * Ràng buộc: Mỗi buổi (Sáng/Chiều) có các tiết thứ tự duy nhất (session, period_number).
 */
@Entity
@Table(name = "time_slots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_timeslot_session_period", columnNames = {"session", "period_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TimeSlot extends BaseEntity {

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "session", length = 20, nullable = false)
    private TimeSlotSession session; // MORNING, AFTERNOON
}

