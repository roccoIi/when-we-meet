package com.whenwemeet.backend.domain.meetingRoom.service;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingCreateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingUpdateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeList;

import java.util.List;

public interface MeetingService {

    List<MeetingListResponse> getAllMeeting (Long userId, SortType type, SortDirection direction);

    void addMeeting(Long userId, MeetingCreateRequest request);

    void updateMeeting(Long userId, MeetingUpdateRequest request);

    void deleteMeeting(Long userId, Long roomId);

    EnterShareLinkResponse getMeetingRoomSummary(String code);

    void enterMeetingRoom(Long userId, String shareCode);

    List<UnavailableTimeList> getAllUnavailableTimeList(String shareCode);
}
