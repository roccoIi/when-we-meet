package com.whenwemeet.backend.domain.schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableDate(
        LocalDate date,
        Long availableCount,
        List<String> availableMembers,
        List<String> unAvailableMembers
) {
}
