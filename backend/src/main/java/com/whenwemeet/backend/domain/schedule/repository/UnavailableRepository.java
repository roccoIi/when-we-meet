package com.whenwemeet.backend.domain.schedule.repository;

import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.custom.UnavailableCustomRepository;
import com.whenwemeet.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UnavailableRepository extends JpaRepository<UnavailableTime, Long>, UnavailableCustomRepository {

    List<UnavailableTimeList> findAllByMeetingRoomAndEndDateTimeGreaterThanEqual(MeetingRoom meetingRoom, LocalDateTime now);

    List<UnavailableTimeList> findAllByMeetingRoomAndUser(MeetingRoom meetingRoom, User user);

    List<UnavailableTime> findAllByUser(User user);

}
