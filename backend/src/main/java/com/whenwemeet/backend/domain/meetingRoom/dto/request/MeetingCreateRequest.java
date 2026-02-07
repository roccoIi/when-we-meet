package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class MeetingCreateRequest {

    private String meetingName;

    private LocalTime startTime;

    private LocalDate startDate;
}
