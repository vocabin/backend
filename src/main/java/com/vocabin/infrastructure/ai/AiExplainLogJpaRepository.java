package com.vocabin.infrastructure.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiExplainLogJpaRepository extends JpaRepository<AiExplainLogEntity, Long> {
    Optional<AiExplainLogEntity> findFirstByMemberIdAndSentenceHashOrderByCreatedAtDesc(Long memberId, String sentenceHash);
    long countByMemberIdAndCreatedAtGreaterThanEqual(Long memberId, LocalDateTime since);
}
