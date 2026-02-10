package com.whenwemeet.backend.domain.schedule.repository;

import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.custom.UnavailableCustomRepository;
import com.whenwemeet.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UnavailableRepository extends JpaRepository<UnavailableTime, Long>, UnavailableCustomRepository {

    List<UnavailableTimeList> findAllByMeetingRoomAndEndDateTimeGreaterThanEqual(MeetingRoom meetingRoom, LocalDateTime now);

    List<UnavailableTimeList> findAllByMeetingRoomAndUser(MeetingRoom meetingRoom, User user);

    List<UnavailableTime> findAllByUser(User user);

    @Modifying(clearAutomatically = true)
    @Query(value = """
                DELETE FROM UnavailableTime u
                WHERE u.meetingRoom.id=:meetingRoomId
                    and u.user.id=:userId""")
    void clearAllScheduleByUser(@Param("userId") Long userId, @Param("meetingRoomId") Long meetingRoomId);

}
