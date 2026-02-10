package com.whenwemeet.backend.domain.schedule.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleRequest(
        LocalDate unavailableDate,
        LocalTime unavailableStartTime,
        LocalTime unavailableEndTime
) {}
