package com.vocabin.domain.ai;

// One expression the AI picked out of a sentence, with a Korean meaning and an example to memo
public record ExpressionSuggestion(
        String expression,
        String meaningKo,
        String exampleEn,
        String exampleKo,
        SuggestionType type
) {}
