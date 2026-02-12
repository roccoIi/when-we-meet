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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface UnavailableRepository extends JpaRepository<UnavailableTime, Long>, UnavailableCustomRepository {

    // 현재 날짜/시간 이후의 불가능한 시간대를 조회
    @Query(value = """
           SELECT u 
           FROM UnavailableTime u 
           WHERE u.meetingRoom = :meetingRoom
           AND (u.unavailableDate > :currentDate
           OR (u.unavailableDate = :currentDate AND u.unavailableEndTime >= :currentTime))""")
    List<UnavailableTimeList> findAllByMeetingRoomAndEndDateTimeGreaterThanEqual(
            @Param("meetingRoom") MeetingRoom meetingRoom, 
            @Param("currentDate") LocalDate currentDate, 
            @Param("currentTime") LocalTime currentTime);

    List<UnavailableTimeList> findAllByMeetingRoomAndUser(MeetingRoom meetingRoom, User user);

    List<UnavailableTime> findAllByUser(User user);

    @Modifying(clearAutomatically = true)
    @Query(value = """
                DELETE FROM UnavailableTime u
                WHERE u.meetingRoom.id=:meetingRoomId
                    and u.user.id=:userId""")
    void clearAllScheduleByUser(@Param("userId") Long userId, @Param("meetingRoomId") Long meetingRoomId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE UnavailableTime u
            SET u.user = :user
            WHERE u.user = :guestUser
            """)
    void updateGuestTimeToUserTime(User user, User guestUser);




}
