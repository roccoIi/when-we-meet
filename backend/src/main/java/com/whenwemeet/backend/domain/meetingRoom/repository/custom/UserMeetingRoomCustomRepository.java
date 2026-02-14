package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

public interface UserMeetingRoomCustomRepository {

    List<MeetingListResponse> findAllByUserId(Long id, Long offset, Long limit, SortType type, SortDirection direction);
    
    Long countByUserId(Long userId);

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomIdisHost(Long userId, Long meetingRoomId, Role role);

//    List<String> findNicknamesByShareCode(String shareCode);

    List<UserInfoResponse> findNicknamesByMeetingRoomId(Long id);

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomIdWithShareCode(Long userId, String shareCode);

    Optional<EnterShareLinkResponse> findNameAndMemberNumberByShareCode(String shareCode);
}
