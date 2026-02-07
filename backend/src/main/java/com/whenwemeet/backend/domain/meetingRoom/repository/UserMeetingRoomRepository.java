package com.whenwemeet.backend.domain.meetingRoom.repository;

import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.repository.custom.UserMeetingRoomCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMeetingRoomRepository extends JpaRepository<UserMeetingRoom, Long>, UserMeetingRoomCustomRepository {

    List<UserMeetingRoom> findAllByUserIdOrderByJoinAtDesc(Long id);

    Long countByMeetingRoomId(Long id);

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomId(Long userId, Long meetingRoomId);
}
