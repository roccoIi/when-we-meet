package com.whenwemeet.backend.domain.meetingRoom.entity.enumType;

import lombok.Getter;

@Getter
public enum DayType {
    MON(1),
    TUE(2),
    WED(3),
    THU(4),
    FRI(5),
    SAT(6),
    SUN(7);

    private final int value;

    DayType(int value) {
        this.value = value;
    }
}
