package com.whenwemeet.backend.domain.meetingRoom.service;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.*;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.*;
import com.whenwemeet.backend.global.response.PageResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface MeetingService {

    PageResponse<List<MeetingListResponse>> getAllMeeting (Long userId, Long page, Long limit, SortType type, SortDirection direction);

    CreateMeetingResponse addMeeting(CustomOAuth2User user, MeetingCreateRequest request, HttpServletResponse response);

    void updateMeeting(CustomOAuth2User user, MeetingUpdateRequest request);

    void deleteMeeting(Long userId, DeleteRoomRequest meetingRoomId);

    EnterShareLinkResponse getMeetingRoomSummary(String code);

    void enterMeetingRoom(Long userId, String shareCode);

    MeetingRoomInfoResponse getMeetingRoomInfoByShareCode(CustomOAuth2User user, String ShareCode);

    MeetingRoomVersionResponse getMeetingRoomVersion(String shareCode);

    void leaveMeeting(CustomOAuth2User user, DeleteRoomRequest request);
}
