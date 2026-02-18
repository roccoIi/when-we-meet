package com.whenwemeet.backend.domain.schedule.service;

import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.repository.MeetingRoomRepository;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.schedule.dto.request.ScheduleRequest;
import com.whenwemeet.backend.domain.schedule.dto.response.DaysDetail;
import com.whenwemeet.backend.domain.schedule.dto.response.MembersScheduleListResponse;
import com.whenwemeet.backend.domain.schedule.dto.response.RecommendList;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.schedule.entity.DayType;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.UnavailableRepository;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.whenwemeet.backend.global.exception.ErrorCode.M002;
import static com.whenwemeet.backend.global.exception.ErrorCode.M003;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

    private final MeetingRoomRepository meetingRoomRepository;
    private final UnavailableRepository unavailableRepository;
    private final UserMeetingRoomRepository userMeetingRoomRepository;
    private final int PLUSDAYS = 90;
    private final int MAX_RECOMMEND_COUNT = 5; // 추천 시간대 개수 (추후 10개로 확장 가능)

    @Override
    public MembersScheduleListResponse getMonthlyAvailableMemberList(String shareCode, int year, int month) {
        // 1) 미팅룸 조회
        MeetingRoom mr = meetingRoomRepository.findAllByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));
        // 2) 현재 미팅룸에 참여중인 인원수 조회
        int allMembersNum = userMeetingRoomRepository.countByMeetingRoom(mr);

        // 3) 월별 시작 및 종료 날짜 정의
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 3-1) startofMonth가 해당 미팅룸의 모임 시작날짜보다 이른 날짜라면 미팅룸의 시작날짜로 변경
        startOfMonth = startOfMonth.isBefore(mr.getStartDate()) ? mr.getStartDate() : startOfMonth;

        // 4) 해당 기간동안 불가능한 사용자를 조회합니다.
        List<UnavailableTime> unavailableTimes = unavailableRepository
                .findAllIncludeInStandardTime(mr, startOfMonth, endOfMonth);

        // 5) 이제 날짜별로 해당 사람들을 분리해야 합니다. (하나의 날짜에 사람 이름이 중복되지 않도록)
        Map<LocalDate, Set<String>> members = new HashMap<>();
        for(UnavailableTime ut : unavailableTimes){
            members.computeIfAbsent(
                    ut.getUnavailableDate(),
                    date -> new HashSet<>()).add(ut.getUser().getNickname());
        }

        // 6) map에 등록된 키를 하나씩 꺼내보면서 저장해야한다. 그럼 현재 일부터 월말까지 순회해야한다.
        List<DaysDetail> MembersScheduleByDate = new ArrayList<>();

        for(LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)){
            // 불가능하다고 표시된 닉네임들 (해당 날짜에 없으면 넘김)
            Set<String> unAvailableNicknames = members.get(date);
            if(unAvailableNicknames == null) continue;

            List<String> UnAvailableMemberList = new ArrayList<>(unAvailableNicknames);

            DaysDetail daysDetail = new DaysDetail(
                    date,
                    allMembersNum - UnAvailableMemberList.size(),
                    UnAvailableMemberList);

            MembersScheduleByDate.add(daysDetail);
        }

        return new MembersScheduleListResponse(allMembersNum, MembersScheduleByDate);
    }

    @Override
    @Transactional
    public void addIndividualSchedule(Long userId, String shareCode, List<ScheduleRequest> scheduleRequest) {

        // 1) 사용자가 해당 미팅룸에 속해있는지 확인 및 User, MeetingRoom 객체 반환
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomShareCode(userId, shareCode)
                .orElseThrow(() -> new NotFoundException(M002));

        // 2) 기존의 스케줄은 모두 삭제
        unavailableRepository.clearAllScheduleByUser(umr.getUser().getId(), umr.getMeetingRoom().getId());

        // 3) 새로 들어온 스케줄을 모두 입력
        List<UnavailableTime> responseList = scheduleRequest.stream()
                .map(sr -> UnavailableTime.builder()
                        .unavailableDate(sr.unavailableDate())
                        .unavailableStartTime(sr.unavailableStartTime())
                        .unavailableEndTime(sr.unavailableEndTime())
                        .user(umr.getUser())
                        .meetingRoom(umr.getMeetingRoom())
                        .build())
                .toList();

        unavailableRepository.saveAll(responseList);
    }

    @Override
    public List<RecommendList> getRecommendSchedule(String shareCode, DayType type) {
        // 1) MeetingRoom 조회
        MeetingRoom meetingRoom = meetingRoomRepository.findAllByShareCode(shareCode)
                .orElseThrow(() -> new NotFoundException(M003));

        // 2) 불가능한 시간대 반환
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<UnavailableTimeList> unavailableTimes = unavailableRepository.findUnavailableTimes(
                        meetingRoom.getId(), meetingRoom.getStartDate(), meetingRoom.getStartTime(), meetingRoom.getEndTime());

        // 2-1) 불가능한 시간대 병합
        List<UnavailableTimeList> mergedUnavailableTimes = mergeUnavailableTimeWithSweepLine(unavailableTimes);

        // 3) 최적의 시간대 찾기 (MAX_RECOMMEND_COUNT개만)
        List<RecommendList> recommendedSlots = new ArrayList<>();
        
        // MeetingRoom의 startDate부터 90일 동안 검색
        LocalDate currentDate = today.isAfter(meetingRoom.getStartDate()) 
                ? today 
                : meetingRoom.getStartDate();
        LocalDate endDate = currentDate.plusDays(PLUSDAYS);
        
        while (!currentDate.isAfter(endDate) && recommendedSlots.size() < MAX_RECOMMEND_COUNT) {
            // DayType에 따른 필터링
            if (!isValidDayType(currentDate, type)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            // MeetingRoom의 startTime ~ endTime 범위 생성
            LocalDateTime dayStart = LocalDateTime.of(currentDate, meetingRoom.getStartTime());
            LocalDateTime dayEnd = LocalDateTime.of(currentDate, meetingRoom.getEndTime());
            
            // 해당 날짜 범위에서 가능한 시간대 계산
            List<RecommendList> dailyAvailableSlots = calculateAvailableSlots(
                currentDate, dayStart, dayEnd, mergedUnavailableTimes
            );
            
            // 해당 날짜의 최적 시간대 선택 (가장 긴 시간대)
            if (!dailyAvailableSlots.isEmpty()) {
                RecommendList longestSlot = findLongestTimeSlot(dailyAvailableSlots);
                recommendedSlots.add(longestSlot);
            }
            
            currentDate = currentDate.plusDays(1);
        }

        // 4) 날짜 및 시작 시간 기준 오름차순 정렬
        recommendedSlots.sort(Comparator
                .comparing(RecommendList::day)
                .thenComparing(RecommendList::startTime));

        return recommendedSlots;
    }

    @Override
    public List<UnavailableTimeList> getAllUnavailableMyTimeList(Long userId, String shareCode) {
        // 1) 유저-미팅룸 조회
        UserMeetingRoom umr = userMeetingRoomRepository.findByUserIdAndMeetingRoomShareCode(userId, shareCode)
                .orElseThrow(() -> new NotFoundException(M002));

        // 2) 사용자가 설정한 모든 불가능한 시간대 반환
        return unavailableRepository
                .findAllByMeetingRoomAndUser(userId, umr.getMeetingRoom().getId());
    }
    
    /**
     * 시간대 리스트 중 가장 긴 시간대를 찾습니다.
     * @param timeSlots 시간대 리스트
     * @return 가장 긴 시간대
     */
    private RecommendList findLongestTimeSlot(List<RecommendList> timeSlots) {
        return timeSlots.stream()
                .max(Comparator.comparingLong(slot -> 
                    java.time.Duration.between(slot.startTime(), slot.endTime()).toMinutes()
                ))
                .orElse(null);
    }
    
    /**
     * DayType에 따라 해당 날짜가 유효한지 확인합니다.
     * @param date 확인할 날짜
     * @param type DayType (ALL, WEEKDAY, WEEKEND)
     * @return 유효한 날짜면 true, 아니면 false
     */
    private boolean isValidDayType(LocalDate date, DayType type) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        
        return switch (type) {
            case ALL -> true;
            case WEEKDAY -> !isWeekend;
            case WEEKEND -> isWeekend;
        };
    }



    /**
     * 특정 날짜 범위에서 불가능한 시간대를 제외한 가능한 시간대를 계산합니다.
     * @param date 계산할 날짜
     * @param dayStart 가능한 날짜 범위 시작 시간
     * @param dayEnd 가능한 날짜 범위 종료 시간
     * @param unavailableTimes 불가능한 시간대 리스트
     * @return 가능한 시간대 리스트
     */
    private List<RecommendList> calculateAvailableSlots(
            LocalDate date,
            LocalDateTime dayStart,
            LocalDateTime dayEnd,
            List<UnavailableTimeList> unavailableTimes) {
        
        List<RecommendList> availableSlots = new ArrayList<>();
        
        // 해당 날짜 범위와 겹치는 불가능한 시간대 필터링
        List<UnavailableTimeList> overlappingUnavailable = unavailableTimes.stream()
                .filter(unavailable -> 
                    // 불가능한 시간이 dayStart ~ dayEnd 범위와 겹치는 경우
                    unavailable.getStartDateTime().isBefore(dayEnd) &&
                    unavailable.getEndDateTime().isAfter(dayStart)
                )
                .sorted(Comparator.comparing(UnavailableTimeList::getStartDateTime))
                .toList();
        
        // 불가능한 시간대가 없으면 전체 범위를 가능한 시간으로 반환
        if (overlappingUnavailable.isEmpty()) {
            availableSlots.add(new RecommendList(
                    date,
                    dayStart.toLocalTime(),
                    dayEnd.toLocalTime()
            ));
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
                availableSlots.add(new RecommendList(
                        date,
                        currentStart.toLocalTime(),
                        unavailableStart.toLocalTime()
                ));
            }
            
            // 다음 가능한 시간 시작점 업데이트
            if (unavailableEnd.isAfter(currentStart)) {
                currentStart = unavailableEnd;
            }
        }
        
        // 마지막 불가능한 시간 이후부터 dayEnd까지가 가능한 시간
        if (currentStart.isBefore(dayEnd)) {
            availableSlots.add(new RecommendList(
                    date,
                    currentStart.toLocalTime(),
                    dayEnd.toLocalTime()
            ));
        }
        
        return availableSlots;
    }

    /**
     * Sweepline 알고리즘을 사용하여 중복되는 시간대를 병합합니다.
     * @param timeList 병합할 시간대 리스트
     * @return 병합된 시간대 리스트
     */
    private List<UnavailableTimeList> mergeUnavailableTimeWithSweepLine(List<UnavailableTimeList> timeList) {
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

            // 현재 시작시간이 이전 종료시간보다 뒤라면 -> 겹치지 않는다.
            if (!current.getStartDateTime().isBefore(end)) {
                // 현재 구간까지 merge에 저장한다.
                mergedList.add(new UnavailableTimeList(start, end));

                // 새로운 구간 시작
                start = current.getStartDateTime();
                end = current.getEndDateTime();
            }
            // 현재 시작시간이 아직 이전구간의 종료시간보다 이전이다 -> 현재 시작시간이 이전구간에 겹쳐있다.
            else {
                // 지금 보는 구간이 과거의 종료기간보다 늦게 끝난다면 더 뒷 값으로 확장
                if (current.getEndDateTime().isAfter(end)) {
                    end = current.getEndDateTime();
                }
            }
        }

        // 5) 마지막 구간 추가
        mergedList.add(new UnavailableTimeList(start, end));

        return mergedList;
    }

}
