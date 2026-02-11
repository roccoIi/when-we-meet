package com.whenwemeet.backend.domain.meetingRoom.service;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingCreateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingUpdateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.CreateMeetingResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingRoomInfoResponse;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.global.response.PageResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface MeetingService {

    PageResponse<List<MeetingListResponse>> getAllMeeting (Long userId, Long page, Long limit, SortType type, SortDirection direction);

    CreateMeetingResponse addMeeting(CustomOAuth2User user, MeetingCreateRequest request, HttpServletResponse response);

    void updateMeeting(CustomOAuth2User user, String shareCode, MeetingUpdateRequest request);

    void deleteMeeting(Long userId, Long roomId);

    EnterShareLinkResponse getMeetingRoomSummary(String code);

    void enterMeetingRoom(Long userId, String shareCode);

    MeetingRoomInfoResponse getMeetingRoomInfoByShareCode(CustomOAuth2User user, String shareCode);

}
