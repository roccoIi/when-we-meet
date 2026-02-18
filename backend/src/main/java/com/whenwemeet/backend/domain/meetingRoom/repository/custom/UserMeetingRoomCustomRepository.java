package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;

import java.util.List;
import java.util.Optional;

public interface UserMeetingRoomCustomRepository {

    List<MeetingListResponse> findAllByUserId(Long id, Long offset, Long limit, SortType type, SortDirection direction);

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomIdisHost(Long userId, Long meetingRoomId, Role role);

    List<UserInfoResponse> findNicknamesByMeetingRoomId(Long id);

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomIdWithShareCode(Long userId, String shareCode);

    Optional<EnterShareLinkResponse> findNameAndMemberNumberByShareCode(String shareCode);
}
