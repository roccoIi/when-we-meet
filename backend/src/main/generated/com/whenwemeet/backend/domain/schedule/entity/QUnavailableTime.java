package com.whenwemeet.backend.domain.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUnavailableTime is a Querydsl query type for UnavailableTime
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUnavailableTime extends EntityPathBase<UnavailableTime> {

    private static final long serialVersionUID = -1948023413L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUnavailableTime unavailableTime = new QUnavailableTime("unavailableTime");

    public final StringPath detail = createString("detail");

    public final DateTimePath<java.time.LocalDateTime> endDateTime = createDateTime("endDateTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.whenwemeet.backend.domain.meetingRoom.entity.QMeetingRoom meetingRoom;

    public final DateTimePath<java.time.LocalDateTime> startDateTime = createDateTime("startDateTime", java.time.LocalDateTime.class);

    public final com.whenwemeet.backend.domain.user.entity.QUser user;

    public QUnavailableTime(String variable) {
        this(UnavailableTime.class, forVariable(variable), INITS);
    }

    public QUnavailableTime(Path<? extends UnavailableTime> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUnavailableTime(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUnavailableTime(PathMetadata metadata, PathInits inits) {
        this(UnavailableTime.class, metadata, inits);
    }

    public QUnavailableTime(Class<? extends UnavailableTime> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.meetingRoom = inits.isInitialized("meetingRoom") ? new com.whenwemeet.backend.domain.meetingRoom.entity.QMeetingRoom(forProperty("meetingRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.whenwemeet.backend.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

