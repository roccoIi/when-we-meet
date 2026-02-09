package com.whenwemeet.backend.domain.meetingRoom.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserMeetingRoom is a Querydsl query type for UserMeetingRoom
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserMeetingRoom extends EntityPathBase<UserMeetingRoom> {

    private static final long serialVersionUID = -1414532234L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserMeetingRoom userMeetingRoom = new QUserMeetingRoom("userMeetingRoom");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> joinAt = createDateTime("joinAt", java.time.LocalDateTime.class);

    public final QMeetingRoom meetingRoom;

    public final EnumPath<com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role> role = createEnum("role", com.whenwemeet.backend.domain.meetingRoom.entity.enumType.Role.class);

    public final com.whenwemeet.backend.domain.user.entity.QUser user;

    public QUserMeetingRoom(String variable) {
        this(UserMeetingRoom.class, forVariable(variable), INITS);
    }

    public QUserMeetingRoom(Path<? extends UserMeetingRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserMeetingRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserMeetingRoom(PathMetadata metadata, PathInits inits) {
        this(UserMeetingRoom.class, metadata, inits);
    }

    public QUserMeetingRoom(Class<? extends UserMeetingRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.meetingRoom = inits.isInitialized("meetingRoom") ? new QMeetingRoom(forProperty("meetingRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.whenwemeet.backend.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

