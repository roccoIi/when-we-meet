package com.whenwemeet.backend.domain.schedule.repository.custom;

import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;

import java.time.LocalDate;
import java.util.List;

public interface UnavailableCustomRepository {
    List<UnavailableTime> findAllIncludeInStandardTime(
            MeetingRoom meetingRoom,
            LocalDate startOfMonth,
            LocalDate endOfMonth
    );
}
