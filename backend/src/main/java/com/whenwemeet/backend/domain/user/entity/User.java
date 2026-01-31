package com.whenwemeet.backend.domain.user.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "users", // 가능하면 user 말고 users 추천(아래 참고)
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
public class User {

    @Id
    @Tsid
    @Column(name = "user_id")
    private Long userId;

    @Column(name="provider")
    private String provider;

    @Column(name = "provider_id")
    private Long providerID;

    @Column(name = "token")
    private Long token;

    @Column(name = "nickname")
    private String nickname;

    @Column(name="profile_pic")
    private String profilePic;

}
