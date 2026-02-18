package com.whenwemeet.backend.domain.meetingRoom.repository.custom;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortDirection;
import com.whenwemeet.backend.domain.meetingRoom.dto.request.SortType;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.EnterShareLinkResponse;
import com.whenwemeet.backend.domain.meetingRoom.dto.response.MeetingListResponse;
import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.whenwemeet.backend.domain.meetingRoom.entity.QMeetingRoom.meetingRoom;
import static com.whenwemeet.backend.domain.meetingRoom.entity.QUserMeetingRoom.userMeetingRoom;
import static com.whenwemeet.backend.domain.user.entity.QUser.user;

@Repository
@AllArgsConstructor
public class UserMeetingRoomCustomRepositoryImpl implements UserMeetingRoomCustomRepository{

    private final JPAQueryFactory factory;



    @Override
    public List<MeetingListResponse> findAllByUserId(Long userId, Long offset, Long limit, SortType type, SortDirection direction) {
        return factory
                .select(
                        Projections.constructor(MeetingListResponse.class,
                                meetingRoom.name,
                                userMeetingRoom.role,
                                JPAExpressions
                                        .select(userMeetingRoom.count())
                                        .from(userMeetingRoom)
                                        .where(userMeetingRoom.meetingRoom.id.eq(meetingRoom.id)),
                                meetingRoom.meetingDate,
                                meetingRoom.shareCode
                        )
                )
                .from(userMeetingRoom)
                .leftJoin(userMeetingRoom.meetingRoom, meetingRoom)
                .where(userMeetingRoom.user.id.eq(userId), meetingRoom.isDeleted.isFalse())
                .orderBy(createOrderSpecifier(type, direction))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<UserMeetingRoom> findByUserIdAndMeetingRoomIdisHost(Long userId, Long meetingRoomId, Role role) {
        UserMeetingRoom result = factory
                .selectFrom(userMeetingRoom)
                .join(userMeetingRoom.meetingRoom, meetingRoom).fetchJoin()
                .where(meetingRoom.id.eq(meetingRoomId)
                        , userMeetingRoom.user.id.eq(userId)
                        , userMeetingRoom.role.eq(role))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<UserInfoResponse> findNicknamesByMeetingRoomId(Long id) {
        return factory
                .select(Projections.constructor(
                        UserInfoResponse.class,
                        user.nickname,
                        user.provider,
                        user.profileImgUrl
                ))
                .from(userMeetingRoom)
                .join(userMeetingRoom.user, user)
                .where(userMeetingRoom.meetingRoom.id.eq(id))
                .fetch();
    }

    @Override
    public Optional<UserMeetingRoom> findByUserIdAndMeetingRoomIdWithShareCode(Long userId, String shareCode) {
        UserMeetingRoom umr =  factory
                .select(userMeetingRoom)
                .from(userMeetingRoom)
                .join(userMeetingRoom.meetingRoom, meetingRoom).fetchJoin()
                .where(userMeetingRoom.user.id.eq(userId), userMeetingRoom.meetingRoom.shareCode.eq(shareCode))
                .fetchOne();

        return Optional.ofNullable(umr);
    }

    @Override
    public Optional<EnterShareLinkResponse> findNameAndMemberNumberByShareCode(String shareCode) {
        EnterShareLinkResponse response = factory
                .select(Projections.constructor(
                        EnterShareLinkResponse.class,
                        meetingRoom.name,
                        userMeetingRoom.count()
                ))
                .from(userMeetingRoom)
                .join(userMeetingRoom.meetingRoom, meetingRoom)
                .where(meetingRoom.isDeleted.eq(false), meetingRoom.shareCode.eq(shareCode))
                .groupBy(meetingRoom.id)
                .fetchOne();

        return Optional.ofNullable(response);
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
