package com.whenwemeet.backend.domain.meetingRoom.repository;

import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingRoomVersionResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    Optional<MeetingRoom> findAllByShareCode(String shareUrl);

    boolean existsByShareCode(String shareUrl);

    Optional<MeetingRoomVersionResponse> findVersionByShareCode(String shareCode);
}