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
        if(user == null) return ResponseEntity.ok(CommonResponse.success());

        PageResponse<List<MeetingListResponse>> response = meetingService.getAllMeeting(user.getId(), page, limit, type, sort);
        return ResponseEntity.ok(CommonResponse.success(response.data(), response.pagination()));
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<?>> addMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody MeetingCreateRequest meetingCreateRequest,
            HttpServletResponse httpServletResponse){
        CreateMeetingResponse response = meetingService.addMeeting(user, meetingCreateRequest, httpServletResponse);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/update")
    public ResponseEntity<CommonResponse<?>> updateMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody MeetingUpdateRequest meetingUpdateRequest)
    {
        meetingService.updateMeeting(user, meetingUpdateRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/{shareCode}")
    public ResponseEntity<CommonResponse<?>> getMeetingRoomInfoByShareCode(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode
    ){
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
        meetingService.deleteMeeting(user.getId(), deleteRoomRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @DeleteMapping("/member")
    public ResponseEntity<CommonResponse<?>> leaveMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody DeleteRoomRequest deleteRoomRequest
    ){
        meetingService.leaveMeeting(user, deleteRoomRequest);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/share/{shareCode}")
    public ResponseEntity<CommonResponse<?>> shareMeeting(
            @PathVariable("shareCode") String shareCode
    ){
        EnterShareLinkResponse response = meetingService.getMeetingRoomSummary(shareCode);
        return ResponseEntity.ok(CommonResponse.success(response));
    }


    @PostMapping("/enter/{shareCode}")
    public ResponseEntity<CommonResponse<?>> enterMeeting(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable("shareCode") String shareCode
    ){
        meetingService.enterMeetingRoom(user.getId(), shareCode);
        return ResponseEntity.ok(CommonResponse.success());
    }
}
