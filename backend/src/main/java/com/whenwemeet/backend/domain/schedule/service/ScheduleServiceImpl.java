package com.whenwemeet.backend.domain.schedule.service;

import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.repository.MeetingRoomRepository;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.schedule.dto.request.ScheduleRequest;
import com.whenwemeet.backend.domain.schedule.dto.response.RecommendList;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeListImpl;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.ScheduleRepository;
import com.whenwemeet.backend.domain.schedule.repository.UnavailableRepository;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.whenwemeet.backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

    private final MeetingRoomRepository meetingRoomRepository;
    private final UnavailableRepository unavailableRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserMeetingRoomRepository userMeetingRoomRepository;
    private final int PLUSDAYS = 90;

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

    @Override
    @Transactional
    public void addIndividualSchedule(Long userId, String shareCode, List<ScheduleRequest> scheduleRequest) {
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomShareCode(userId, shareCode)
                .orElseThrow(() -> new NotFoundException(M002));


        List<UnavailableTime> responseList = scheduleRequest.stream()
                .map(sr -> UnavailableTime.builder()
                        .startDateTime(sr.getStartDateTime())
                        .endDateTime(sr.getEndDateTime())
                        .user(umr.getUser())
                        .meetingRoom(umr.getMeetingRoom())
                        .build())
                .toList();
        unavailableRepository.saveAll(responseList);
    }

    @Override
    public List<RecommendList> getRecommendSchedule(String shareCode) {
        // 1) UserMeetingRoom 검증 (권한 확인)
        MeetingRoom meetingRoom = meetingRoomRepository.findAllByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));

        // 2) 불가능한 시간대 병합
        List<UnavailableTimeList> unavailableTimes = unavailableRepository
                .findAllByMeetingRoomAndEndDateTimeGreaterThanEqual(meetingRoom, LocalDateTime.now());
        List<UnavailableTimeList> mergedUnavailableTimes = mergeOverlappingTimeIntervals(unavailableTimes);

        // 3) 가능한 시간대 계산
        List<RecommendList> availableTimeSlots = new ArrayList<>();
        
        // startDate부터 90일 동안 검색
        LocalDate currentDate = LocalDate.now().isAfter(meetingRoom.getStartDate()) ? LocalDate.now() : meetingRoom.getStartDate();
        LocalDate endDate = currentDate.plusDays(PLUSDAYS);
        
        while (!currentDate.isAfter(endDate)) {
            // 각 날짜의 startTime ~ endTime 범위 생성
            LocalDateTime dayStart = LocalDateTime.of(currentDate, meetingRoom.getStartTime());
            LocalDateTime dayEnd = LocalDateTime.of(currentDate, meetingRoom.getEndTime());
            
            // 해당 날짜 범위에서 가능한 시간대 계산
            List<RecommendList> dailyAvailableSlots = calculateAvailableSlots(
                dayStart, dayEnd, mergedUnavailableTimes
            );
            
            availableTimeSlots.addAll(dailyAvailableSlots);
            currentDate = currentDate.plusDays(1);
        }

        // 4) 시작 시간 기준 오름차순 정렬
        availableTimeSlots.sort(Comparator.comparing(RecommendList::getStartDateTime));

        return availableTimeSlots;
    }

    @Override
    public List<UnavailableTimeList> getAllUnavailableMyTimeList(Long userId, String shareCode) {
        // 1) 유저-미팅룸 조회
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomShareCode(userId, shareCode)
                .orElseThrow(() -> new NotFoundException(M002));

        // 2) 사용자가 설정한 모든 불가능한 시간대 반환
        return unavailableRepository
                .findAllByMeetingRoomAndUser(umr.getMeetingRoom(), umr.getUser());
    }

    /**
     * 특정 날짜 범위에서 불가능한 시간대를 제외한 가능한 시간대를 계산합니다.
     * @param dayStart 날짜 범위 시작 시간
     * @param dayEnd 날짜 범위 종료 시간
     * @param unavailableTimes 불가능한 시간대 리스트
     * @return 가능한 시간대 리스트
     */
    private List<RecommendList> calculateAvailableSlots(
            LocalDateTime dayStart,
            LocalDateTime dayEnd,
            List<UnavailableTimeList> unavailableTimes) {
        
        List<RecommendList> availableSlots = new ArrayList<>();
        
        // 해당 날짜 범위와 겹치는 불가능한 시간대 필터링
        List<UnavailableTimeList> overlappingUnavailable = unavailableTimes.stream()
                .filter(unavailable -> 
                    // 불가능한 시간이 dayStart ~ dayEnd 범위와 겹치는 경우
                    !unavailable.getEndDateTime().isBefore(dayStart) && 
                    !unavailable.getStartDateTime().isAfter(dayEnd)
                )
                .sorted(Comparator.comparing(UnavailableTimeList::getStartDateTime))
                .toList();
        
        // 불가능한 시간대가 없으면 전체 범위를 가능한 시간으로 반환
        if (overlappingUnavailable.isEmpty()) {
            availableSlots.add(RecommendList.builder()
                    .startDateTime(dayStart)
                    .endDateTime(dayEnd)
                    .build());
            return availableSlots;
        }
        
        // 가능한 시간대 계산
        LocalDateTime currentStart = dayStart;
        
        for (UnavailableTimeList unavailable : overlappingUnavailable) {
            LocalDateTime unavailableStart = unavailable.getStartDateTime();
            LocalDateTime unavailableEnd = unavailable.getEndDateTime();
            
            // 불가능한 시간이 범위보다 앞에 있으면 조정
            if (unavailableStart.isBefore(dayStart)) {
                unavailableStart = dayStart;
            }
            
            // 불가능한 시간이 범위보다 뒤에 있으면 조정
            if (unavailableEnd.isAfter(dayEnd)) {
                unavailableEnd = dayEnd;
            }
            
            // currentStart부터 불가능한 시간 시작까지가 가능한 시간
            if (currentStart.isBefore(unavailableStart)) {
                availableSlots.add(RecommendList.builder()
                        .startDateTime(currentStart)
                        .endDateTime(unavailableStart)
                        .build());
            }
            
            // 다음 가능한 시간 시작점 업데이트
            if (unavailableEnd.isAfter(currentStart)) {
                currentStart = unavailableEnd;
            }
        }
        
        // 마지막 불가능한 시간 이후부터 dayEnd까지가 가능한 시간
        if (currentStart.isBefore(dayEnd)) {
            availableSlots.add(RecommendList.builder()
                    .startDateTime(currentStart)
                    .endDateTime(dayEnd)
                    .build());
        }
        
        return availableSlots;
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
        timeList.sort(Comparator.comparing(UnavailableTimeList::getStartDateTime));

        // 2) 병합 결과를 저장할 리스트
        List<UnavailableTimeList> mergedList = new ArrayList<>();

        // 3) 첫 번째 시간대로 초기화
        LocalDateTime start = timeList.get(0).getStartDateTime();
        LocalDateTime end = timeList.get(0).getEndDateTime();

        // 4) Sweepline 알고리즘 적용
        for (int i = 1; i < timeList.size(); i++) {
            UnavailableTimeList current = timeList.get(i);

            // 현재 구간과 다음 구간이 겹치거나 인접한 경우
            if (current.getStartDateTime().isAfter(end)) {
                // 겹치지 않는 경우: 현재까지의 병합된 구간을 결과에 추가
                mergedList.add(new UnavailableTimeListImpl(start, end));

                // 새로운 구간 시작
                start = current.getStartDateTime();
                end = current.getEndDateTime();
            } else {
                // 끝 시간을 더 큰 값으로 확장
                if (current.getEndDateTime().isAfter(end)) {
                    end = current.getEndDateTime();
                }
            }
        }

        // 5) 마지막 구간 추가
        mergedList.add(new UnavailableTimeListImpl(start, end));

        for(UnavailableTimeList unavailableTime : mergedList) {
            log.info(unavailableTime.toString());
        }

        return mergedList;
    }
}
