package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public record MeetingListResponse (
        String name,
        Role role,
        Long memberNumber,
        LocalDateTime meetingDate,
        String shareCode
){}