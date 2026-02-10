package com.whenwemeet.backend.domain.schedule.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.schedule.dto.response.UnavailableTimeList;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.whenwemeet.backend.domain.schedule.entity.QUnavailableTime.unavailableTime;
import static com.whenwemeet.backend.domain.user.entity.QUser.user;


@Repository
@AllArgsConstructor
public class UnavailableCustomRepositoryImpl implements UnavailableCustomRepository{

    private final JPAQueryFactory factory;


    @Override
    public List<UnavailableTime> findAllIncludeInStandardTime(
            MeetingRoom meetingRoom, LocalDate startOfMonth, LocalDate endOfMonth) {

        return factory
                .select(unavailableTime)
                .from(unavailableTime)
                .join(unavailableTime.user, user).fetchJoin()
                .where(
                        unavailableTime.meetingRoom.eq(meetingRoom),
                        unavailableTime.unavailableDate.between(startOfMonth, endOfMonth),
                        unavailableTime.unavailableStartTime.lt(meetingRoom.getEndTime()),
                        unavailableTime.unavailableEndTime.goe(meetingRoom.getStartTime())
                )
                .fetch();
    }
}
