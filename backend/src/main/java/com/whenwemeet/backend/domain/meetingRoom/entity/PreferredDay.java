package com.whenwemeet.backend.domain.meetingRoom.entity;

import com.whenwemeet.backend.domain.meetingRoom.entity.enumType.DayType;
import jakarta.persistence.*;

@Entity
public class PreferredDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dayType")
    private DayType dayType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MeetingRoom meetingRoom;

}
