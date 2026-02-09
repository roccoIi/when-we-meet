package com.whenwemeet.backend.domain.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendList {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
