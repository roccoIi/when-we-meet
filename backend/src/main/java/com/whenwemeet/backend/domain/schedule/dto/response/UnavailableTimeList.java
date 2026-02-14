package com.whenwemeet.backend.domain.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnavailableTimeList {
    LocalDate unavailableDate;
    LocalTime unavailableStartTime;
    LocalTime unavailableEndTime;

    public UnavailableTimeList(LocalDateTime start, LocalDateTime end) {
        this.unavailableDate = start.toLocalDate();
        this.unavailableStartTime = start.toLocalTime();
        this.unavailableEndTime = end.toLocalTime();
    }
    
    // 기존 코드와의 호환성을 위한 default 메서드
    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(getUnavailableDate(), getUnavailableStartTime());
    }
    
    public LocalDateTime getEndDateTime() {
        return LocalDateTime.of(getUnavailableDate(), getUnavailableEndTime());
    }
}
