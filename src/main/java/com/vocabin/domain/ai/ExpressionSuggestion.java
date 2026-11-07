package com.vocabin.domain.ai;

import java.util.List;

// One expression the AI picked out of a sentence. The first meaning is the one used in that sentence,
// with the learner's own sentence as its example; the rest are other common meanings.
public record ExpressionSuggestion(
        String expression,
        SuggestionType type,
        List<Meaning> meanings
) {
    public ExpressionSuggestion {
        meanings = meanings == null ? List.of() : List.copyOf(meanings);
    }

    public record Meaning(String meaningKo, String exampleEn, String exampleKo, boolean fromSentence) {}
}
