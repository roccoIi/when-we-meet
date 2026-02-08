package com.whenwemeet.backend.domain.meetingRoom.repository;

import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UnavailableTime;
import com.whenwemeet.backend.domain.meetingRoom.repository.custom.UnavailableCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UnavailableRepository extends JpaRepository<UnavailableTime, Long>, UnavailableCustomRepository {

    List<UnavailableTimeList> findAllByMeetingRoomAndEndDateTimeGreaterThanEqual(MeetingRoom meetingRoom, LocalDateTime now);
}
