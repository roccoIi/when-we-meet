package com.whenwemeet.backend.domain.meetingRoom.entity;

import com.whenwemeet.backend.global.entity.BaseEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE meeting_room SET is_deleted = true WHERE id = ?")
public class MeetingRoom extends BaseEntity {

    @Id
    @Tsid
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "member_number")
    private Long memberNumber = 1L;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    @Column(name = "share_code")
    private String shareCode;

    public void changeSetting(String name, LocalDate meetingDate){
        if(name != null) this.name = name;
        if(meetingDate != null) this.meetingDate = meetingDate;
    }

    public void addMember(){
        this.memberNumber++;
    }

    public void removeMember(){
        this.memberNumber--;
    }
}
