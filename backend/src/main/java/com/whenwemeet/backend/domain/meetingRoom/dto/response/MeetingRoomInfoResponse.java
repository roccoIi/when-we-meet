package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record MeetingRoomInfoResponse(
        Long id,
        String name,
        Role role,
        Integer memberNumber,
        LocalDateTime confirmDate, // 확정일자
        List<UserInfoResponse> info,
        LocalDate meetingDate, // 희망 모임 시작일자
        LocalTime startTime, // 희망 모임 시작시간
        LocalTime endTime, // 희망 모임 끝시간
        Long version // 버전정보
) {}


