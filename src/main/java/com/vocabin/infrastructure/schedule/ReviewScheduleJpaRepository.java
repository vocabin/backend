package com.vocabin.infrastructure.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewScheduleJpaRepository extends JpaRepository<ReviewScheduleEntity, Long> {
    Optional<ReviewScheduleEntity> findByWordId(Long wordId);

    @Query("SELECT rs.word.id FROM ReviewScheduleEntity rs WHERE rs.nextReviewAt <= :today AND rs.word.wordSet.memberId = :memberId ORDER BY rs.nextReviewAt ASC")
    List<Long> findDueWordIds(@Param("today") LocalDate today, @Param("memberId") Long memberId);

    @Query("SELECT rs.word.id FROM ReviewScheduleEntity rs WHERE rs.word.wordSet.memberId = :memberId")
    Set<Long> findAllScheduledWordIds(@Param("memberId") Long memberId);
}
