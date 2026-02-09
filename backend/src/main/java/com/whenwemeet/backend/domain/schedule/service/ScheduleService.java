package com.whenwemeet.backend.domain.schedule.service;

import com.whenwemeet.backend.domain.schedule.dto.request.ScheduleRequest;
import com.whenwemeet.backend.domain.schedule.dto.response.RecommendList;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;

import java.util.List;

public interface ScheduleService {

    List<UnavailableTimeList> getAllUnavailableTimeList(String shareCode);

    void addIndividualSchedule(Long userId, String shareCode, List<ScheduleRequest> scheduleRequest);

    List<RecommendList> getRecommendSchedule(String shareCode);

    List<UnavailableTimeList> getAllUnavailableMyTimeList(Long userId, String shareCode);
}
