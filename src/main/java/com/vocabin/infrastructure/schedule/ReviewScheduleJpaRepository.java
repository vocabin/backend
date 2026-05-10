package com.vocabin.infrastructure.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewScheduleJpaRepository extends JpaRepository<ReviewScheduleEntity, Long> {
    Optional<ReviewScheduleEntity> findByWordId(Long wordId);
}
