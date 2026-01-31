package com.whenwemeet.backend.global.redis;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refreshToken")
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long id;
    private String refreshToken;

    @TimeToLive
    private Long timeToLive;
}
