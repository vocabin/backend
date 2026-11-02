package com.vocabin.application.service;

import com.vocabin.domain.ai.ExpressionSuggestion;

import java.util.List;

public interface AiExpressionService {
    Result explain(Long memberId, String sentence);

    record Result(String sentence, List<ExpressionSuggestion> suggestions, boolean cached, int remainingToday) {}
}
