package com.whenwemeet.backend.domain.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UnavailableTimeListImpl implements UnavailableTimeList {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
