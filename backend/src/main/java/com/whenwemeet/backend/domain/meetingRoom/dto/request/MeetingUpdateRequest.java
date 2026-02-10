package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import java.time.LocalDateTime;

public record MeetingUpdateRequest(
        String name,
        LocalDateTime meetingDate
){}

