package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;

import java.util.List;

public interface UserMeetingRoomCustomRepository {

    List<MeetingListResponse> findAllByUserId(Long id, Long offset, Long limit, SortType type, SortDirection direction);
    
    Long countByUserId(Long userId);

//    List<String> findNicknamesByShareCode(String shareCode);
}
