package com.whenwemeet.backend.domain.meetingRoom.dto.request;

public record DeleteRoomRequest(
        Long id,
        Long version
){}
