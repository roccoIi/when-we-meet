package com.whenwemeet.backend.domain.schedule.controller;

import com.whenwemeet.backend.domain.schedule.dto.request.ScheduleRequest;
import com.whenwemeet.backend.domain.schedule.dto.response.MembersScheduleListResponse;
import com.whenwemeet.backend.domain.schedule.dto.response.RecommendList;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.schedule.entity.DayType;
import com.whenwemeet.backend.domain.schedule.service.ScheduleService;
import com.whenwemeet.backend.global.response.CommonResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/available/{shareCode}")
    public ResponseEntity<CommonResponse<?>> getMeetingSchedule(
            @PathVariable("shareCode") String shareCode,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ){
        log.info("[월별 멤버 가용성 조회] shareCode: {}, year: {}, month: {}", shareCode, year, month);
        MembersScheduleListResponse response = scheduleService.getMonthlyAvailableMemberList(shareCode, year, month);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/my/{shareCode}")
    public ResponseEntity<CommonResponse<?>> getMySchedule(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode
    ){
        log.info("[이용자 개인의 불가능한 시간 리스트 반환]");
        List<UnavailableTimeList> response = scheduleService.getAllUnavailableMyTimeList(user.getId(), shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/{shareCode}")
    public ResponseEntity<CommonResponse<?>> addMeetingSchedule(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode,
            @RequestBody List<ScheduleRequest> scheduleRequest
    ){
        log.info("[현재 미팅룸에 불가능한 스케줄 저장]");
        scheduleService.addIndividualSchedule(user.getId(), shareCode, scheduleRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/recommend/{shareCode}/{type}")
    public ResponseEntity<CommonResponse<?>> getRecommendMeetingSchedule(
            @PathVariable("shareCode") String shareCode,
            @PathVariable("type") DayType type
    ){
        log.info("[가장 추천되는 날짜 및 시간 반환]");
        List<RecommendList> response = scheduleService.getRecommendSchedule(shareCode, type);
        return ResponseEntity.ok(CommonResponse.success(response));
    }


}
