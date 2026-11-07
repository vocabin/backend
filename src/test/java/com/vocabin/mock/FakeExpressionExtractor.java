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
    private String promptVersion = "test-v1";

    public void failNextCalls() {
        failing = true;
    }

    public void returnEmptyResults() {
        empty = true;
    }

    public void changePromptVersion(String version) {
        promptVersion = version;
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
        return List.of(new ExpressionSuggestion("come across", SuggestionType.PHRASAL_VERB, List.of(
                new ExpressionSuggestion.Meaning("우연히 만나다", sentence.value(), "어제 옛 친구를 우연히 만났다.", true),
                new ExpressionSuggestion.Meaning("~한 인상을 주다", "He comes across as shy.", "그는 수줍어 보여.", false))));
    }

    @Override
    public String promptVersion() {
        return promptVersion;
    }
}
