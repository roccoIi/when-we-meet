package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MeetingListResponse {

    private String name;
    private Role role;
    private Long memberNumber;
    private LocalDateTime meetingDate;
    private String shareCode;
}