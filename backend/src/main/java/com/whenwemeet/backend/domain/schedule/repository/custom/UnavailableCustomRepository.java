package com.whenwemeet.backend.domain.schedule.repository.custom;

import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;

import java.util.List;

public interface UnavailableCustomRepository {
    List<UnavailableTimeList> findUnavailableTimeListByMeetingId(Long meetingId);
}
