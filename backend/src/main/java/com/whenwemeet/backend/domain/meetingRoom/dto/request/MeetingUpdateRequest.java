package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public record MeetingUpdateRequest(
        String name,
        LocalDateTime meetingDate
){}

