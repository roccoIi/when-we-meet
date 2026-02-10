package com.whenwemeet.backend.domain.schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DaysDetail(
        LocalDate date,
        Integer availableCount,
        List<String> unAvailableMembers
) {
}
