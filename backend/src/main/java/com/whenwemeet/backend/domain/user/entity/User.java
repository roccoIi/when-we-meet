package com.whenwemeet.backend.domain.user.entity;

import com.whenwemeet.backend.global.entity.BaseEntity;
import com.whenwemeet.backend.global.util.RandomNickname;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
public class User extends BaseEntity {

    @Id
    @Tsid
    @Column(name = "id")
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Column(name="provider")
    private String provider;

    @Column(name = "provider_id")
    private Long providerID;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserType role;

    @Column(name="profile_img_url")
    private String profileImgUrl;

    public static User createGuest(){
        return User.builder()
                .nickname(new RandomNickname().generateNickname())
                .role(UserType.GUEST)
                .build();
    }

    public void changeNickName(String nickname){
        this.nickname = nickname;
    }

    public void updateNewUser(User user){
        this.provider = user.provider;
        this.providerID = user.providerID;
        this.nickname = user.nickname;
        this.role = user.role;
        this.profileImgUrl = user.profileImgUrl;
    }
}
