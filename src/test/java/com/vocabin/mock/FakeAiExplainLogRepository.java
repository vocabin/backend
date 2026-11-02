package com.vocabin.mock;

import com.vocabin.application.port.out.AiExplainLogRepository;
import com.vocabin.domain.ai.AiExplainLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeAiExplainLogRepository implements AiExplainLogRepository {

    private final List<AiExplainLog> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Optional<AiExplainLog> findLatestByMemberIdAndSentenceHash(Long memberId, String sentenceHash) {
        return store.stream()
                .filter(log -> log.getMemberId().equals(memberId) && log.getSentenceHash().equals(sentenceHash))
                .max(Comparator.comparing(AiExplainLog::getCreatedAt));
    }

    @Override
    public long countByMemberIdSince(Long memberId, LocalDateTime since) {
        return store.stream()
                .filter(log -> log.getMemberId().equals(memberId) && !log.getCreatedAt().isBefore(since))
                .count();
    }

    @Override
    public AiExplainLog save(AiExplainLog log) {
        AiExplainLog saved = AiExplainLog.builder()
                .id(idGenerator.incrementAndGet())
                .memberId(log.getMemberId())
                .sentenceHash(log.getSentenceHash())
                .sentence(log.getSentence())
                .suggestions(log.getSuggestions())
                .createdAt(log.getCreatedAt())
                .build();
        store.add(saved);
        return saved;
    }
}
