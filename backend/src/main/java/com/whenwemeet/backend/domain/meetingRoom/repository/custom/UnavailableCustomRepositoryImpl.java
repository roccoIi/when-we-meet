package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.UnavailableTimeList;
import static com.whenwemeet.backend.domain.meetingRoom.entity.QMeetingRoom.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class UnavailableCustomRepositoryImpl implements UnavailableCustomRepository{

    private final JPAQueryFactory factory;

    @Override
    public List<UnavailableTimeList> findUnavailableTimeListByMeetingId(Long shareCode) {
        return null;
    }
}
