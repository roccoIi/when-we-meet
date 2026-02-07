package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;

import java.util.List;

public interface UserMeetingRoomCustomRepository {

    List<MeetingListResponse> findAllByUserId(Long id, SortType type, SortDirection direction);
}
