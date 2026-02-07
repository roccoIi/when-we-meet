package com.whenwemeet.backend.domain.user.repository.custom;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whenwemeet.backend.domain.user.dto.response.UserInfoResponse;
import static com.whenwemeet.backend.domain.user.entity.QUser.user;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository{

    private final JPAQueryFactory factory;

    @Override
    public Optional<UserInfoResponse> findInfoByUserId(Long userId) {
        return Optional.ofNullable(factory
                .select(Projections.fields(UserInfoResponse.class
                    ,user.nickname
                    ,user.profileImgUrl))
                .from(user)
                .where(user.id.eq(userId))
                .fetchOne());
    }
}
