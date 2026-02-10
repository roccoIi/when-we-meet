package com.whenwemeet.backend.domain.schedule.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface UnavailableTimeList {
    LocalDate getUnavailableDate();
    LocalTime getUnavailableStartTime();
    LocalTime getUnavailableEndTime();
    
    // 기존 코드와의 호환성을 위한 default 메서드
    default LocalDateTime getStartDateTime() {
        return LocalDateTime.of(getUnavailableDate(), getUnavailableStartTime());
    }
    
    default LocalDateTime getEndDateTime() {
        return LocalDateTime.of(getUnavailableDate(), getUnavailableEndTime());
    }
}
