package com.whenwemeet.backend.domain.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class UnavailableTimeListImpl implements UnavailableTimeList {
    private LocalDate unavailableDate;
    private LocalTime unavailableStartTime;
    private LocalTime unavailableEndTime;
    
    // LocalDateTime을 받아서 분리하는 편의 생성자
    public UnavailableTimeListImpl(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.unavailableDate = startDateTime.toLocalDate();
        this.unavailableStartTime = startDateTime.toLocalTime();
        this.unavailableEndTime = endDateTime.toLocalTime();
    }
}
