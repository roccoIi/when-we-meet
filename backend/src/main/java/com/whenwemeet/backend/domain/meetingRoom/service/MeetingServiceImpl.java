package com.whenwemeet.backend.domain.meetingRoom.service;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.*;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.*;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.meetingRoom.repository.MeetingRoomRepository;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.schedule.repository.UnavailableRepository;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.entity.Pagination;
import com.whenwemeet.backend.global.exception.type.DuplicateException;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.response.PageResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.whenwemeet.backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService{
    private static final int MAX_RETRY = 10;
    private final UserRepository userRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final UserMeetingRoomRepository userMeetingRoomRepository;
    private final UnavailableRepository unavailableRepository;

    @Override
    public PageResponse<List<MeetingListResponse>> getAllMeeting(Long userId, Long page, Long limit, SortType type, SortDirection direction) {
        // 1) 해당 유저가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        // 2) offset 계산 (page는 1부터 시작)
        long offset = (page - 1) * limit;

        // 3) 해당 유저가 참여중인 미팅룸 전체 조회
        List<MeetingListResponse> meetingList = userMeetingRoomRepository.findAllByUserId(user.getId(), offset, limit, type, direction);

        // 4) 전체 개수 조회
        long totalItems = meetingList.size();

        // 5) 페이지네이션 정보 계산
        long totalPages = (long) Math.ceil((double) totalItems / limit);
        boolean hasMore = page < totalPages;

        Pagination pagination = new Pagination(page, totalPages, totalItems, hasMore);

        return new PageResponse<>(meetingList, pagination);
    }

    @Override
    @Transactional
    public CreateMeetingResponse addMeeting(CustomOAuth2User user, MeetingCreateRequest rq, HttpServletResponse response) {

        // 2) 미팅룸 생성
        MeetingRoom mr = MeetingRoom.builder()
                .name(rq.meetingName())
                .startDate(rq.startDate())
                .startTime(rq.startTime())
                .endTime(rq.endTime())
                .shareCode(generateShareCode())
                .build();

        // 2) 미팅룸 저장
        meetingRoomRepository.save(mr);

        // 4) 유저 - 미팅룽 매칭
        UserMeetingRoom umr = UserMeetingRoom.builder()
                .role(Role.HOST)
                .joinAt(LocalDateTime.now())
                .user(user.getUser())
                .meetingRoom(mr)
                .build();

        // 5) 매칭정보 저장
        userMeetingRoomRepository.save(umr);

        return new CreateMeetingResponse(mr.getShareCode());
    }

    @Override
    @Transactional
    public void updateMeeting(CustomOAuth2User user, MeetingUpdateRequest request) {

        // 1) 요청이 들어온 방이 존재하는지 확인
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomId(user.getId(), request.id())
                .orElseThrow(() -> new NotFoundException(M002));

        // 2) 현재 사용자가 해당 미팅룸의 호스트인지 확인
        if(umr.getRole() != Role.HOST){
            throw new NotFoundException(M001);
        }

        // 3) 설정 변경 진행 (모든 필드 지원)
        umr.getMeetingRoom().changeSetting(
                request.name(),
                request.meetingDate(),
                request.startDate(),
                request.startTime(),
                request.endTime()
        );
    }

    @Override
    @Transactional
    public void deleteMeeting(Long userId, DeleteRoomRequest request) {
        // 1) 권한체크
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomIdisHost(userId, request.id(), Role.HOST)
                        .orElseThrow(() -> new NotFoundException(M002));

        // 2) 삭제 진행 (Soft Delete)
        userMeetingRoomRepository.deleteAllUserInMeetingRoom(request.id());
        unavailableRepository.deleteAllTimeInMeetingRoom(request.id());
        meetingRoomRepository.delete(umr.getMeetingRoom());
    }

    @Override
    @Transactional
    public void leaveMeeting(CustomOAuth2User user, DeleteRoomRequest request) {
        // 1) 권한체크
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomIdisHost(user.getId(), request.id(), Role.MEMBER)
                .orElseThrow(() -> new NotFoundException(M002));

        userMeetingRoomRepository.deleteUserInMeetingRoom(user.getId(), request.id());

        unavailableRepository.deleteTimeInMeetingRoom(user.getId(), request.id());
    }

    @Override
    public EnterShareLinkResponse getMeetingRoomSummary(String code) {
        return userMeetingRoomRepository.findNameAndMemberNumberByShareCode(code)
                .orElseThrow(() -> new NotFoundException(M005));
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

        // 5) 저장
        userMeetingRoomRepository.save(umr);

        // 6) shareCount 수정 (만약 0이 됐다면 shareCode 새롭게 발급)
        room.minusShareCount();
        if(room.getShareCount() == 0){
            room.updateShareCode(generateShareCode());
            room.initializeShareCount();
        }
    }

    @Override
    public MeetingRoomInfoResponse getMeetingRoomInfoByShareCode(CustomOAuth2User user, String ShareCode) {
        // 1) 요청이 들어온 user와 meetingroom 권한체크
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomIdWithShareCode(user.getId(), ShareCode)
                .orElseThrow(() -> new NotFoundException(M002));

        // 2) 해당 미팅룸에 속한 모든 User의 nickname 조회
        List<UserInfoResponse> infos = userMeetingRoomRepository.findNicknamesByMeetingRoomId(umr.getMeetingRoom().getId());

        // 3) 응답 DTO 생성
        return new MeetingRoomInfoResponse(
                umr.getMeetingRoom().getId(),
                umr.getMeetingRoom().getName(),
                umr.getRole(),
                infos.size(),
                umr.getMeetingRoom().getMeetingDate(),
                infos,
                umr.getMeetingRoom().getStartDate(),
                umr.getMeetingRoom().getStartTime(),
                umr.getMeetingRoom().getEndTime(),
                umr.getMeetingRoom().getVersion()
        );
    }

    @Override
    public MeetingRoomVersionResponse getMeetingRoomVersion(String shareCode) {
        return meetingRoomRepository.findVersionByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));
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
