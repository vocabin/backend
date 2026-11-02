package com.vocabin.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.AiExplainLogRepository;
import com.vocabin.domain.ai.AiExplainLog;
import com.vocabin.domain.ai.ExpressionSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AiExplainLogRepositoryImpl implements AiExplainLogRepository {

    private static final TypeReference<List<ExpressionSuggestion>> SUGGESTIONS = new TypeReference<>() {};

    private final AiExplainLogJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<AiExplainLog> findLatestByMemberIdAndSentenceHash(Long memberId, String sentenceHash) {
        return jpaRepository.findFirstByMemberIdAndSentenceHashOrderByCreatedAtDesc(memberId, sentenceHash)
                .map(this::toModel);
    }

    @Override
    public long countByMemberIdSince(Long memberId, LocalDateTime since) {
        return jpaRepository.countByMemberIdAndCreatedAtGreaterThanEqual(memberId, since);
    }

    @Override
    public AiExplainLog save(AiExplainLog log) {
        try {
            AiExplainLogEntity saved = jpaRepository.save(AiExplainLogEntity.of(
                    log.getId(), log.getMemberId(), log.getSentenceHash(), log.getSentence(),
                    objectMapper.writeValueAsString(log.getSuggestions()), log.getCreatedAt()));
            return toModel(saved);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize AI suggestions", e);
        }
    }

    private AiExplainLog toModel(AiExplainLogEntity entity) {
        try {
            return AiExplainLog.builder()
                    .id(entity.getId())
                    .memberId(entity.getMemberId())
                    .sentenceHash(entity.getSentenceHash())
                    .sentence(entity.getSentence())
                    .suggestions(objectMapper.readValue(entity.getSuggestionsJson(), SUGGESTIONS))
                    .createdAt(entity.getCreatedAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot read stored AI suggestions", e);
        }
    }
}
