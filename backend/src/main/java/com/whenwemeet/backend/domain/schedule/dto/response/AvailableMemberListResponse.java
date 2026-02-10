package com.whenwemeet.backend.domain.schedule.dto.response;

import java.util.List;

public record AvailableMemberListResponse(
        Integer totalMembers,
        List<AvailableDate> AvailableDateList
) {
}
