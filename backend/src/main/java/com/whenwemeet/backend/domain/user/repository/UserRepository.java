package com.whenwemeet.backend.domain.user.repository;

import com.whenwemeet.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByProviderAndProviderID(String provider, Long providerId);

    Optional<User> findUserByUserId(Long userId);
}
