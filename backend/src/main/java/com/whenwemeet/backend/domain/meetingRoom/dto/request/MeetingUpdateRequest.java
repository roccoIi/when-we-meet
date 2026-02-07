package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MeetingUpdateRequest {

    private Long id;
    private String name;
    private LocalDate meetingDate;

}
