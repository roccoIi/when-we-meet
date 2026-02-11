package com.whenwemeet.backend.domain.meetingRoom.repository;

import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    Optional<EnterShareLinkResponse> findByShareCode(String shareUrl);

    Optional<MeetingRoom> findAllByShareCode(String shareUrl);

    boolean existsByShareCode(String shareUrl);

    @Query(value = """
                SELECT mr.name as name, count(umr.user) as memberNumber
                FROM MeetingRoom mr
                JOIN UserMeetingRoom umr on umr.meetingRoom = mr
                WHERE mr.shareCode = :shareCode AND mr.isDeleted = false
                group by umr.meetingRoom
                """)
    Optional<EnterShareLinkResponse> findNameAndMemberNumberByShareCode(@Param("shareCode") String shareCode);
}