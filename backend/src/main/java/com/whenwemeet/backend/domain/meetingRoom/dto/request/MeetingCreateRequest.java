package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record MeetingCreateRequest(
        String meetingName,
        LocalTime startTime,
        LocalDate startDate,
        LocalTime endTime
) {}
