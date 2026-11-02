package com.vocabin.mock;

import com.vocabin.application.port.out.ExpressionExtractor;
import com.vocabin.domain.ai.EnglishSentence;
import com.vocabin.domain.ai.ExpressionSuggestion;
import com.vocabin.domain.ai.SuggestionType;

import java.util.List;

// Stands in for the paid AI: counts calls so tests can prove when the AI is (not) called
public class FakeExpressionExtractor implements ExpressionExtractor {

    private int calls = 0;
    private boolean failing = false;
    private boolean empty = false;

    public void returnEmptyResults() {
        empty = true;
    }

    public void failNextCalls() {
        failing = true;
    }

    public int calls() {
        return calls;
    }

    @Override
    public List<ExpressionSuggestion> extract(EnglishSentence sentence) {
        calls++;
        if (failing) {
            throw new ExpressionExtractionException("fake failure", null);
        }
        if (empty) {
            return List.of();
        }
        return List.of(new ExpressionSuggestion(
                "come across", "우연히 발견하다", "I came across an old photo.", "옛날 사진을 우연히 발견했다.",
                SuggestionType.PHRASAL_VERB));
    }
}
