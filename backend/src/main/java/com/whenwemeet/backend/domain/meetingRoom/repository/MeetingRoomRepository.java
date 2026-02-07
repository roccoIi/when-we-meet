package com.whenwemeet.backend.domain.meetingRoom.repository;

import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    Optional<EnterShareLinkResponse> findByShareCode(String shareUrl);

    Optional<MeetingRoom> findAllByShareCode(String shareUrl);

    boolean existsByShareCode(String shareUrl);
}
