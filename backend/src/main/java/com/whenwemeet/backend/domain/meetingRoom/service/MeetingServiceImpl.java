package com.whenwemeet.backend.domain.meetingRoom.service;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingCreateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingUpdateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeListImpl;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.meetingRoom.repository.MeetingRoomRepository;
import com.whenwemeet.backend.domain.meetingRoom.repository.UnavailableRepository;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import static com.whenwemeet.backend.global.exception.ErrorCode.*;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final UnavailableRepository unavailableRepository;

    @Override
    public List<MeetingListResponse> getAllMeeting(Long userId, SortType type, SortDirection direction) {
        // 1) 해당 유저가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        // 2) 해당 유저가 참여중인 미팅룸 전체 조회
        return userMeetingRoomRepository.findAllByUserId(user.getId(), type, direction);
    }

    @Override
    @Transactional
    public void addMeeting(Long userId, MeetingCreateRequest rq) {
        // 1) 미팅룸 생성
        MeetingRoom mr = MeetingRoom.builder()
                .name(rq.getMeetingName())
                .startDate(rq.getStartDate())
                .startTime(rq.getStartTime())
                .shareCode(generateShareCode())
                .build();

        // 2) 미팅룸 저장
        meetingRoomRepository.save(mr);

        // 3) 유저 객체 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(U001));

        // 4) 유저 - 미팅룽 매칭
        UserMeetingRoom umr = UserMeetingRoom.builder()
                .role(Role.HOST)
                .joinAt(LocalDateTime.now())
                .user(user)
                .meetingRoom(mr)
                .build();

        // 5) 매칭정보 저장
        userMeetingRoomRepository.save(umr);
    }

    @Override
    @Transactional
    public void updateMeeting(Long userId, MeetingUpdateRequest request) {

        // 1) 요청이 들어온 방이 존재하는지 확인
        MeetingRoom meetingRoom = meetingRoomRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(M003));

        // 2) 현재 사용자가 해당 미팅룸의 호스트인지 확인
        if(hostCheck(userId, meetingRoom.getId())) throw new NotFoundException(M001);

        // 3) 설정 변경 진행
        meetingRoom.changeSetting(request.getName(), request.getMeetingDate());
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

        // 3) 유저-미팅룸 객체 생성
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
    public List<UnavailableTimeList> getAllUnavailableTimeList(String shareCode) {
        // 1) 미팅룸 조회
        MeetingRoom room = meetingRoomRepository.findAllByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));

        // 2) 오늘 날짜 이후의 모든 불가능한 시간대 조회
        List<UnavailableTimeList> times = unavailableRepository
                .findAllByMeetingRoomAndEndDateTimeGreaterThanEqual(room, LocalDateTime.now());

        // 3) Sweepline 알고리즘을 사용하여 중복되는 시간대 병합
        return mergeOverlappingTimeIntervals(times);
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

    /**
     * Sweepline 알고리즘을 사용하여 중복되는 시간대를 병합합니다.
     * @param timeList 병합할 시간대 리스트
     * @return 병합된 시간대 리스트
     */
    private List<UnavailableTimeList> mergeOverlappingTimeIntervals(List<UnavailableTimeList> timeList) {
        // 빈 리스트인 경우 그대로 반환
        if (timeList == null || timeList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1) 시작 시간 기준으로 정렬
        List<UnavailableTimeList> sortedTimes = new ArrayList<>(timeList);
        sortedTimes.sort(Comparator.comparing(UnavailableTimeList::getStartTime));

        // 2) 병합 결과를 저장할 리스트
        List<UnavailableTimeList> mergedList = new ArrayList<>();

        // 3) 첫 번째 시간대로 초기화
        LocalDateTime start = sortedTimes.get(0).getStartTime();
        LocalDateTime end = sortedTimes.get(0).getEndTime();

        // 4) Sweepline 알고리즘 적용
        for (int i = 1; i < sortedTimes.size(); i++) {
            UnavailableTimeList current = sortedTimes.get(i);
            
            // 현재 구간과 다음 구간이 겹치거나 인접한 경우
            if (current.getStartTime().isAfter(end)) {
                // 겹치지 않는 경우: 현재까지의 병합된 구간을 결과에 추가
                mergedList.add(new UnavailableTimeListImpl(start, end));

                // 새로운 구간 시작
                start = current.getStartTime();
                end = current.getEndTime();
            } else {
                // 끝 시간을 더 큰 값으로 확장
                if (current.getEndTime().isAfter(end)) {
                    end = current.getEndTime();
                }
            }
        }

        // 5) 마지막 구간 추가
        mergedList.add(new UnavailableTimeListImpl(start, end));

        return mergedList;
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
