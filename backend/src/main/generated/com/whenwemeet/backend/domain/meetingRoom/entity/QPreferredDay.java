package com.whenwemeet.backend.domain.meetingRoom.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPreferredDay is a Querydsl query type for PreferredDay
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPreferredDay extends EntityPathBase<PreferredDay> {

    private static final long serialVersionUID = 1125702832L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPreferredDay preferredDay = new QPreferredDay("preferredDay");

    public final EnumPath<com.whenwemeet.backend.domain.meetingRoom.entity.enumType.DayType> dayType = createEnum("dayType", com.whenwemeet.backend.domain.meetingRoom.entity.enumType.DayType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMeetingRoom meetingRoom;

    public QPreferredDay(String variable) {
        this(PreferredDay.class, forVariable(variable), INITS);
    }

    public QPreferredDay(Path<? extends PreferredDay> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPreferredDay(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPreferredDay(PathMetadata metadata, PathInits inits) {
        this(PreferredDay.class, metadata, inits);
    }

    public QPreferredDay(Class<? extends PreferredDay> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.meetingRoom = inits.isInitialized("meetingRoom") ? new QMeetingRoom(forProperty("meetingRoom")) : null;
    }

}

