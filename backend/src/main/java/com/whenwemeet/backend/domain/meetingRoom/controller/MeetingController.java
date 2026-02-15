package com.whenwemeet.backend.domain.meetingRoom.controller;

import com.whenwemeet.backend.domain.meetingRoom.dto.request.*;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.*;
import com.whenwemeet.backend.domain.meetingRoom.service.MeetingService;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.global.response.CommonResponse;
import com.whenwemeet.backend.global.response.PageResponse;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping()
    public ResponseEntity<CommonResponse<?>> findAllMeetings(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "limit", defaultValue = "10") Long limit,
            @RequestParam(name = "type", defaultValue = "JOIN_DATE") SortType type,
            @RequestParam(name = "sort", defaultValue = "DESC") SortDirection sort) {
        log.info("[미팅룸 리스트 조회] page={}, limit={}", page, limit);
        if(user == null) return ResponseEntity.ok(CommonResponse.success());

        PageResponse<List<MeetingListResponse>> response = meetingService.getAllMeeting(user.getId(), page, limit, type, sort);
        return ResponseEntity.ok(CommonResponse.success(response.data(), response.pagination()));
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<?>> addMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody MeetingCreateRequest meetingCreateRequest,
            HttpServletResponse httpServletResponse){
        log.info("[미팅 생성]");
        CreateMeetingResponse response = meetingService.addMeeting(user, meetingCreateRequest, httpServletResponse);
        log.info(response.shareCode());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/update")
    public ResponseEntity<CommonResponse<?>> updateMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody MeetingUpdateRequest meetingUpdateRequest)
    {
        log.info("[미팅 수정]");
        meetingService.updateMeeting(user, meetingUpdateRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/{shareCode}")
    public ResponseEntity<CommonResponse<?>> getMeetingRoomInfoByShareCode(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode
    ){
        log.info("[미팅룸 정보 반환] shareCode={}", shareCode);
        MeetingRoomInfoResponse response = meetingService.getMeetingRoomInfoByShareCode(user, shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/{shareCode}/version")
    public ResponseEntity<CommonResponse<?>> getMeetingRoomVersion(
            @PathVariable("shareCode") String shareCode
    ){
        MeetingRoomVersionResponse response = meetingService.getMeetingRoomVersion(shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @DeleteMapping("/host")
    public ResponseEntity<CommonResponse<?>> deleteMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody DeleteRoomRequest deleteRoomRequest)
    {
        log.info("[미팅 삭제(Host)] SoftDelete 진행");
        meetingService.deleteMeeting(user.getId(), deleteRoomRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @DeleteMapping("/member")
    public ResponseEntity<CommonResponse<?>> leaveMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody DeleteRoomRequest deleteRoomRequest
    ){
        log.info("[미팅 탈퇴]");
        meetingService.leaveMeeting(user, deleteRoomRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/share/{shareCode}")
    public ResponseEntity<CommonResponse<?>> shareMeeting(
            @PathVariable("shareCode") String shareCode
    ){
        log.info("[공유링크 접속 후 미팅룸 요약정보 반환]");
        EnterShareLinkResponse response = meetingService.getMeetingRoomSummary(shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }


    @PostMapping("/enter/{shareCode}")
    public ResponseEntity<CommonResponse<?>> enterMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode
    ){
        log.info("[미팅룸 입장 및 관계매핑]");
        meetingService.enterMeetingRoom(user.getId(), shareCode);
        return ResponseEntity.ok(CommonResponse.success());
    }


}
