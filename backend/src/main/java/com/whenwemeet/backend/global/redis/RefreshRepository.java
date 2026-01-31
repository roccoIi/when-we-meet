package com.whenwemeet.backend.global.redis;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshRepository extends CrudRepository<RefreshToken, Long> {

}
