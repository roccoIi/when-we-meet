package com.whenwemeet.backend.domain.meetingRoom.repository;

import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.meetingRoom.repository.custom.UserMeetingRoomCustomRepository;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import com.whenwemeet.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMeetingRoomRepository extends JpaRepository<UserMeetingRoom, Long>, UserMeetingRoomCustomRepository {

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomId(Long userId, Long meetingRoomId);

    Optional<UserMeetingRoom> findByUserIdAndMeetingRoomShareCode(Long userId, String shareCode);

    Optional<UserMeetingRoom> findByUserAndMeetingRoomShareCode(User user, String shareCode);

    List<UserMeetingRoom> findAllByUser(User user);

    List<UserMeetingRoom> findAllByMeetingRoom(MeetingRoom meetingRoom);

    Integer countByMeetingRoom(MeetingRoom meetingRoom);

    Integer countByMeetingRoomShareCode(String shareCode);

    // from절 서브쿼리가 더 성능이 좋을 것으로 생각되어 우선 sql문으로 작성
    // 쓰기-읽기 요청 비교하여 만일 쓰기가 월등히 적다면 index생성 후 jpql/queryDsl로 변경하는게 가독성 측면에서 우위
    // 만일 쓰기요청도 읽기 못지 않게 많다면 인덱스 생성 안하는게 나을 수도?? 이건 고민해볼문제
    @Query(
            value = """
           select u.nickname, u.provider, u.profile_img_url
           from (select mr.id as mrid
                 from meeting_room as mr
                 where mr.share_code=:shareCode and mr.is_deleted=false) as custom_mr
           join user_meeting_room as umr on umr.meeting_room_id=custom_mr.mrid
           join users as u on umr.user_id=u.id""",
            nativeQuery = true)
    List<UserInfoResponse> findNicknamesByShareCode(@Param("shareCode") String shareCode);

    boolean existsByUserAndMeetingRoom(User user, MeetingRoom meetingRoom);
}
