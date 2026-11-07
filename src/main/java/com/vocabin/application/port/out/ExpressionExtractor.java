package com.vocabin.application.port.out;

import com.vocabin.domain.ai.EnglishSentence;
import com.vocabin.domain.ai.ExpressionSuggestion;

import java.util.List;

public interface ExpressionExtractor {
    // Throws ExpressionExtractionException when the AI provider is unavailable or misconfigured
    List<ExpressionSuggestion> extract(EnglishSentence sentence);

    // Changes whenever the prompt or response shape changes, so cached results of an older version are not reused
    String promptVersion();

    class ExpressionExtractionException extends RuntimeException {
        public ExpressionExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
