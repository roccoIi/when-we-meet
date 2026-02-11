package com.whenwemeet.backend.domain.schedule.dto.response;

import java.util.List;

public record MembersScheduleListResponse(
        Integer totalMembers,
        List<DaysDetail> MembersScheduleByDate
) {
}
