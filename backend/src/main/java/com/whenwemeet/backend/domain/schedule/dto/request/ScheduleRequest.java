package com.whenwemeet.backend.domain.schedule.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleRequest {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
