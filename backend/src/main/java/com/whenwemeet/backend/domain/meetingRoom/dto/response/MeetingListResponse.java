package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MeetingListResponse {

    private String name;
    private Role role;
    private Long memberNumber;
    private LocalDate meetingDate;
    private String shareCode;
}