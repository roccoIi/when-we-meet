package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MeetingListResponse {

    private String name;
    private Long memberNumber;
    private LocalDate meetingDate;
}