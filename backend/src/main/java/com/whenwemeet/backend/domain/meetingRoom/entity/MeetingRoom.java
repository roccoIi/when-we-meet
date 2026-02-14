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
@SQLDelete(sql = "UPDATE meeting_room SET is_deleted = true, version = version + 1 WHERE id = ? AND version = ?")
public class MeetingRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "meeting_date")
    private LocalDateTime meetingDate;

    @Column(name = "share_code")
    private String shareCode;

    @Builder.Default
    @Column(name = "share_count")
    private Integer shareCount = 30;

    @Version
    @Column(name = "version")
    private Long version;
    
    public void changeSetting(String name, LocalDateTime meetingDate, LocalDate startDate, LocalTime startTime, LocalTime endTime){
        if(name != null) this.name = name;
        this.meetingDate = meetingDate;
        if(startDate != null) this.startDate = startDate;
        if(startTime != null) this.startTime = startTime;
        if(endTime != null) this.endTime = endTime;
    }

    public void minusShareCount(){
        this.shareCount--;
    }

    public void updateShareCode(String shareCode){
        this.shareCode = shareCode;
    }

    public void initializeShareCount(){
        this.shareCount = 30;
    }
}
