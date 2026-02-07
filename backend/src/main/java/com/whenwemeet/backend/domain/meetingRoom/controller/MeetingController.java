package com.whenwemeet.backend.domain.meetingRoom.controller;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingCreateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.MeetingUpdateRequest;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.meetingRoom.service.MeetingService;
import com.whenwemeet.backend.global.response.CommonResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meeting")
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping()
    public ResponseEntity<CommonResponse<?>> findAllMeetings(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestParam(name = "type", defaultValue = "JOIN_DATE") SortType type,
            @RequestParam(name = "sort", defaultValue = "DESC") SortDirection sort) {
        log.info("[미팅룸 리스트 조회]");
        List<MeetingListResponse> response = meetingService.getAllMeeting(user.getUserId(), type, sort);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<?>> addMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody MeetingCreateRequest meetingCreateRequest){
        log.info("[미팅 생성]");
        meetingService.addMeeting(user.getUserId(), meetingCreateRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @PutMapping()
    public ResponseEntity<CommonResponse<?>> updateMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody MeetingUpdateRequest meetingUpdateRequest)
    {
        log.info("[미팅 수정]");
        meetingService.updateMeeting(user.getUserId(), meetingUpdateRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<CommonResponse<?>> deleteMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable Long roomId)
    {
        log.info("[미팅 삭제] SoftDelete 진행");
        meetingService.deleteMeeting(user.getUserId(), roomId);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/share/{shareCode}")
    public ResponseEntity<CommonResponse<?>> shareMeeting(
            @PathVariable String shareCode
    ){
        log.info("[공유링크 접속 후 미팅룸 요약정보 반환]");
        EnterShareLinkResponse response = meetingService.getMeetingRoomSummary(shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/enter/{shareCode}")
    public ResponseEntity<CommonResponse<?>> enterMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable String shareCode
    ){
        log.info("[미팅룸 입장 및 관계매핑]");
        meetingService.enterMeetingRoom(user.getUserId(), shareCode);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/schedule/{shareCode}")
    public ResponseEntity<CommonResponse<?>> getMeetingSchedule(
            @PathVariable String shareCode
    ){
        log.info("[현재 미팅룸에 저장된 불가능한 시간 리스트 반환]");
        List<UnavailableTimeList> response = meetingService.getAllUnavailableTimeList(shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
