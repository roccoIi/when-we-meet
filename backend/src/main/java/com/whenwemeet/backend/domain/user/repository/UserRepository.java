package com.whenwemeet.backend.domain.user.repository;

import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.custom.UserCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    Optional<User> findUserByProviderAndProviderID(String provider, String providerId);

    Optional<User> findUserById(Long userId);
}
