package com.vocabin.application.port.out;

import com.vocabin.domain.ai.AiExplainLog;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiExplainLogRepository {
    Optional<AiExplainLog> findLatestByMemberIdAndSentenceHash(Long memberId, String sentenceHash);
    long countByMemberIdSince(Long memberId, LocalDateTime since);
    AiExplainLog save(AiExplainLog log);
}
