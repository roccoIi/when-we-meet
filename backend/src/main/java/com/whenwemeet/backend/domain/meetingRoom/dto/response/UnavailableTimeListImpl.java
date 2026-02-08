package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UnavailableTimeListImpl implements UnavailableTimeList {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
