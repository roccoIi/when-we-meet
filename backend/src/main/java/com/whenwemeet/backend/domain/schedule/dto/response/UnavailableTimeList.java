package com.whenwemeet.backend.domain.meetingRoom.dto.response;

import com.whenwemeet.backend.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

public interface UnavailableTimeList {
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
