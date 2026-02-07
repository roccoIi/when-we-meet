package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import static com.whenwemeet.backend.domain.meetingRoom.entity.QUserMeetingRoom.*;
import static com.whenwemeet.backend.domain.meetingRoom.entity.QMeetingRoom.*;

import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class UserMeetingRoomCustomRepositoryImpl implements UserMeetingRoomCustomRepository{

    private final JPAQueryFactory factory;



    @Override
    public List<MeetingListResponse> findAllByUserId(Long userId, SortType type, SortDirection direction) {
        return factory
                .select(
                        Projections.fields(MeetingListResponse.class,
                                meetingRoom.name,
                                meetingRoom.memberNumber,
                                meetingRoom.meetingDate)
                )
                .from(userMeetingRoom)
                .join(userMeetingRoom.meetingRoom, meetingRoom)
                .where(userMeetingRoom.user.id.eq(userId))
                .orderBy(createOrderSpecifier(type, direction))
                .fetch();
    }


    private OrderSpecifier<?>[] createOrderSpecifier(SortType type, SortDirection direction) {

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        Order order = SortDirection.ASC.equals(direction) ? Order.ASC : Order.DESC;

        switch (type){
            case NAME -> orderSpecifiers.add(new OrderSpecifier<>(order, meetingRoom.name));
            case JOIN_DATE -> orderSpecifiers.add(new OrderSpecifier<>(order, userMeetingRoom.joinAt));
            case MEETING_DATE -> orderSpecifiers.add(new OrderSpecifier<>(order, meetingRoom.meetingDate));
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }
}
