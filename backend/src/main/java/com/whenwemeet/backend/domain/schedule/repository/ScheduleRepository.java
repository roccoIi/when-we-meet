package com.whenwemeet.backend.domain.schedule.repository;

import com.whenwemeet.backend.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
