package com.whenwemeet.backend.domain.schedule.service;

import com.whenwemeet.backend.domain.schedule.dto.request.ScheduleRequest;
import com.whenwemeet.backend.domain.schedule.dto.response.RecommendList;
import com.whenwemeet.backend.domain.schedule.dto.response.MembersScheduleListResponse;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.schedule.entity.DayType;
import com.whenwemeet.backend.domain.user.entity.User;

import java.util.List;

public interface ScheduleService {

    MembersScheduleListResponse getMonthlyAvailableMemberList(String shareCode, int year, int month);

    void addIndividualSchedule(Long userId, String shareCode, List<ScheduleRequest> scheduleRequest);

    List<RecommendList> getRecommendSchedule(String shareCode, DayType type);

    List<UnavailableTimeList> getAllUnavailableMyTimeList(Long userId, String shareCode);

//    public User mergeUserAndGuestUserInfo(User user, User guestUser);
}
