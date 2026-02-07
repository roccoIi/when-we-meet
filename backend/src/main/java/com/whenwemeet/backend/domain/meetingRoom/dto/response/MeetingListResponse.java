package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingListResponse {

    private String meetingName;

    private Long memberNumber;

    private LocalDate meetingDay;
}
