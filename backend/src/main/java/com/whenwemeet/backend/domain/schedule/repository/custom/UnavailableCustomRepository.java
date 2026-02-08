package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeList;

import java.util.List;

public interface UnavailableCustomRepository {
    List<UnavailableTimeList> findUnavailableTimeListByMeetingId(Long meetingId);
}
