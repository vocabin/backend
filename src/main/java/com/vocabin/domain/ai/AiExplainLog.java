package com.vocabin.domain.ai;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// One paid AI call: doubles as the result cache and the daily usage counter
@Getter
@Builder
public class AiExplainLog {

    private final Long id;
    private final Long memberId;
    private final String sentenceHash;
    private final String sentence;
    private final List<ExpressionSuggestion> suggestions;
    private final LocalDateTime createdAt;

    public static AiExplainLog record(Long memberId, EnglishSentence sentence, List<ExpressionSuggestion> suggestions,
                                      ClockHolder clockHolder) {
        return AiExplainLog.builder()
                .memberId(memberId)
                .sentenceHash(sentence.cacheKey())
                .sentence(sentence.value())
                .suggestions(List.copyOf(suggestions))
                .createdAt(clockHolder.now())
                .build();
    }
}
