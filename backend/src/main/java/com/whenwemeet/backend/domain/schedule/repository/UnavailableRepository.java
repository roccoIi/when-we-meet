package com.whenwemeet.backend.domain.schedule.repository;

import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.custom.UnavailableCustomRepository;
import com.whenwemeet.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnavailableRepository extends JpaRepository<UnavailableTime, Long>, UnavailableCustomRepository {

    List<UnavailableTime> findAllByUser(User user);

    @Modifying(clearAutomatically = true)
    @Query(value = """
                DELETE FROM UnavailableTime u
                WHERE u.meetingRoom.id=:meetingRoomId
                    and u.user.id=:userId""")
    void clearAllScheduleByUser(@Param("userId") Long userId, @Param("meetingRoomId") Long meetingRoomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete UnavailableTime u
            where u.meetingRoom.id = :meetingRoomId""")
    void deleteAllTimeInMeetingRoom(Long meetingRoomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete UnavailableTime u
            where u.meetingRoom.id = :meetingRoomId AND u.user.id = :userId""")
    void deleteTimeInMeetingRoom(Long userId, Long meetingRoomId);


}
