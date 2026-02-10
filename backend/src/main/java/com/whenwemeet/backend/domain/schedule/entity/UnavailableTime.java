package com.whenwemeet.backend.domain.schedule.entity;

import com.whenwemeet.backend.domain.meetingRoom.entity.MeetingRoom;
import com.whenwemeet.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UnavailableTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String detail;

    @Column(nullable = false)
    private LocalDate unavailableDate;

    @Column(nullable = false)
    private LocalTime unavailableStartTime;

    @Column(nullable = false)
    private LocalTime unavailableEndTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MeetingRoom meetingRoom;


    public void changeUser(User user) {
        this.user = user;
    }
}
