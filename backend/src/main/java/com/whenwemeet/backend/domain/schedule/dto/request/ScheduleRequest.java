package com.whenwemeet.backend.domain.schedule.dto.request;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public record ScheduleRequest(
        LocalDate unavailableDate,
        LocalTime unavailableStartTime,
        LocalTime unavailableEndTime
) {}
