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
        MembersScheduleListResponse response = scheduleService.getMonthlyAvailableMemberList(shareCode, year, month);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/my/{shareCode}")
    public ResponseEntity<CommonResponse<?>> getMySchedule(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode
    ){
        List<UnavailableTimeList> response = scheduleService.getAllUnavailableMyTimeList(user.getId(), shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/{shareCode}")
    public ResponseEntity<CommonResponse<?>> addMeetingSchedule(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode,
            @RequestBody List<ScheduleRequest> scheduleRequest
    ){
        scheduleService.addIndividualSchedule(user.getId(), shareCode, scheduleRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/recommend/{shareCode}/{type}")
    public ResponseEntity<CommonResponse<?>> getRecommendMeetingSchedule(
            @PathVariable("shareCode") String shareCode,
            @PathVariable("type") DayType type
    ){
        List<RecommendList> response = scheduleService.getRecommendSchedule(shareCode, type);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
