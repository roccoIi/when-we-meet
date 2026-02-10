package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MeetingUpdateRequest{
    private final String name;
    private final LocalDateTime meetingDate;

}

