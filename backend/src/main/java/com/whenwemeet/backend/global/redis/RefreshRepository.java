package com.whenwemeet.backend.global.redis;

import org.springframework.data.repository.CrudRepository;

public interface RefreshRepository extends CrudRepository<RefreshToken, Long> {

}
