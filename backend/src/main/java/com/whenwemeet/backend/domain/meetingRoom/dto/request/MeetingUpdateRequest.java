package com.whenwemeet.backend.domain.meetingRoom.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MeetingUpdateRequest(
        String name,
        LocalDateTime meetingDate, // 확정일자 (nullable)
        LocalDate startDate, // 희망 시작 날짜 (nullable)
        LocalTime startTime, // 희망 시작 시간 (nullable)
        LocalTime endTime // 희망 종료 시간 (nullable)
){}

