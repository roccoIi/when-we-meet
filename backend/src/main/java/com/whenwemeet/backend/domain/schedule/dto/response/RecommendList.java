package com.whenwemeet.backend.domain.schedule.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record RecommendList(
        LocalDate day,
        LocalTime startTime,
        LocalTime endTime
) {}
