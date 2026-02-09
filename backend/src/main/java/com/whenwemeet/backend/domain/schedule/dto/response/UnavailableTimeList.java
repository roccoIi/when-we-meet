package com.whenwemeet.backend.domain.schedule.dto.response;

import java.time.LocalDateTime;

public interface UnavailableTimeList {
    LocalDateTime getStartDateTime();
    LocalDateTime getEndDateTime();
}
