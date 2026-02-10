package com.whenwemeet.backend.domain.meetingRoom.service;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingCreateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingUpdateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.CreateMeetingResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingRoomInfoResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.meetingRoom.repository.MeetingRoomRepository;
import com.whenwemeet.backend.domain.schedule.repository.UnavailableRepository;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.entity.Pagination;
import com.whenwemeet.backend.global.exception.type.DuplicateException;
import com.whenwemeet.backend.global.jwt.util.JwtUtil;
import com.whenwemeet.backend.global.response.PageResponse;
import static com.whenwemeet.backend.global.exception.ErrorCode.*;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService{
    private static final int MAX_RETRY = 10;
    private final UserRepository userRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final UserMeetingRoomRepository userMeetingRoomRepository;
    private final JwtUtil jwtUtil;

    @Override
    public PageResponse<List<MeetingListResponse>> getAllMeeting(Long userId, Long page, Long limit, SortType type, SortDirection direction) {
        // 1) 해당 유저가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        // 2) offset 계산 (page는 1부터 시작)
        Long offset = (page - 1) * limit;

        // 3) 해당 유저가 참여중인 미팅룸 전체 조회
        List<MeetingListResponse> meetingList = userMeetingRoomRepository.findAllByUserId(user.getId(), offset, limit, type, direction);

        // 4) 전체 개수 조회
        Long totalItems = userMeetingRoomRepository.countByUserId(user.getId());

        // 5) 페이지네이션 정보 계산
        long totalPages = (long) Math.ceil((double) totalItems / limit);
        boolean hasMore = page < totalPages;

        Pagination pagination = new Pagination(page, totalPages, totalItems, hasMore);

        return new PageResponse<>(meetingList, pagination);
    }

    @Override
    @Transactional
    public CreateMeetingResponse addMeeting(CustomOAuth2User user, MeetingCreateRequest rq, HttpServletResponse response) {

        // 1) 유저객체 반환 (만일 이용기록 없는 사용자가 생성을 요청했더면 우선 User 객체먼저 생성)
        User finalUser;
        if(user == null){
            finalUser = User.createGuest();
            userRepository.save(finalUser);

            jwtUtil.generateAccessToken(finalUser.getId(), response);
            jwtUtil.generateRefreshToken(finalUser.getId(), response);
        } else {
            finalUser = userRepository.findById(user.getId()).orElseThrow(() -> new NotFoundException(U001));
        }

        // 2) 미팅룸 생성
        MeetingRoom mr = MeetingRoom.builder()
                .name(rq.getMeetingName())
                .startDate(rq.getStartDate())
                .startTime(rq.getStartTime())
                .shareCode(generateShareCode())
                .build();

        // 2) 미팅룸 저장
        meetingRoomRepository.save(mr);

        // 4) 유저 - 미팅룽 매칭
        UserMeetingRoom umr = UserMeetingRoom.builder()
                .role(Role.HOST)
                .joinAt(LocalDateTime.now())
                .user(finalUser)
                .meetingRoom(mr)
                .build();

        // 5) 매칭정보 저장
        userMeetingRoomRepository.save(umr);

        return new CreateMeetingResponse(mr.getShareCode());
    }

    @Override
    @Transactional
    public void updateMeeting(CustomOAuth2User user, String shareCode, MeetingUpdateRequest request) {

        // 1) 요청이 들어온 방이 존재하는지 확인
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserAndMeetingRoomShareCode(user.getUser(), shareCode)
                .orElseThrow(() -> new NotFoundException(M002));

        // 2) 현재 사용자가 해당 미팅룸의 호스트인지 확인
        if(hostCheck(user.getId(), umr.getMeetingRoom().getId())) throw new NotFoundException(M001);

        // 3) 설정 변경 진행
        umr.getMeetingRoom().changeSetting(request.getName(), request.getMeetingDate());
    }

    @Override
    @Transactional
    public void deleteMeeting(Long userId, Long roomId) {

        // 1) 현재 사용자가 해당 미팅룸의 호스트인지 확인
        if(hostCheck(userId, roomId)) throw new NotFoundException(M002);

        // 2) 삭제 진행 (Soft Delete)
        meetingRoomRepository.deleteById(roomId);
    }

    @Override
    public EnterShareLinkResponse getMeetingRoomSummary(String code) {
        return meetingRoomRepository.findByShareCode(code)
                .orElseThrow(() -> new NotFoundException(M003));
    }

    @Override
    @Transactional
    public void enterMeetingRoom(Long userId, String shareCode) {

        // 1) 유저객체 탐색
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        // 2) 미팅룸 객체 탐색
        MeetingRoom room = meetingRoomRepository.findAllByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));

        // 3) 이미 매핑되어있는지 확인
        if(userMeetingRoomRepository.existsByUserAndMeetingRoom(user, room)) {
            throw new DuplicateException(M004);
        }

        // 4) 유저-미팅룸 객체 생성
        UserMeetingRoom umr = UserMeetingRoom.builder()
                .role(Role.MEMBER)
                .joinAt(LocalDateTime.now())
                .user(user)
                .meetingRoom(room)
                .build();

        // 4) 미팅룸 인원 추가(+1)
        room.addMember();

        // 5) 저장
        userMeetingRoomRepository.save(umr);
    }

    @Override
    public MeetingRoomInfoResponse getMeetingRoomInfoByShareCode(String shareCode) {
        
        // 1) shareCode로 MeetingRoom 조회
        MeetingRoom meetingRoom = meetingRoomRepository.findAllByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));
        
        // 2) 해당 미팅룸에 속한 모든 User의 nickname 조회
        List<UserInfoResponse> infos = userMeetingRoomRepository.findNicknamesByShareCode(shareCode);
        
        // 3) 응답 DTO 생성
        return MeetingRoomInfoResponse.builder()
                .name(meetingRoom.getName())
                .memberNumber(meetingRoom.getMemberNumber())
                .info(infos)
                .build();
    }


    /**
     * 현재 사용자가 해당 미팅룸의 호스트인지 확인합니다.
     * @param userId 사용자 PK
     * @param roomId MeetingRoom PK
     * @return 불일치할 경우 True 반환, 일치할경우 False 반환
     */
    private boolean hostCheck(Long userId, Long roomId) {
        return userMeetingRoomRepository.findByUserIdAndMeetingRoomId(userId, roomId)
                .orElseThrow(() -> new NotFoundException(M002))
                .getRole() != Role.HOST;
    }

    private String generateShareCode() {
        for(int i = 0; i < MAX_RETRY; i++){
            String code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 13);

            if(!meetingRoomRepository.existsByShareCode(code)){
                return code;
            }
        }
        throw new NotFoundException(T003);
    }
    
}
