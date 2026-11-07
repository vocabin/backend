package com.vocabin.api.ai;

import com.vocabin.application.service.AiExpressionService;
import com.vocabin.domain.ai.ExpressionSuggestion;
import com.vocabin.domain.ai.SuggestionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 표현 추출 결과")
public record ExplainResponse(
        @Schema(description = "분석한 문장 (공백 정리됨)") String sentence,
        @Schema(description = "추천 표현 목록 (최대 6개)") List<Suggestion> suggestions,
        @Schema(description = "같은 문장의 저장된 결과를 재사용했는지 (true면 비용 없음)") boolean cached,
        @Schema(description = "오늘 남은 AI 호출 횟수") int remainingToday
) {
    @Schema(description = "추천 표현")
    public record Suggestion(
            @Schema(description = "표현 (기본형)", example = "go through") String expression,
            @Schema(description = "종류: IDIOM, PHRASAL_VERB, EXPRESSION, GRAMMAR, VOCABULARY") SuggestionType type,
            @Schema(description = "뜻 목록 (첫 번째가 이 문장에서 쓰인 뜻)") List<Meaning> meanings
    ) {
        static Suggestion from(ExpressionSuggestion s) {
            return new Suggestion(s.expression(), s.type(), s.meanings().stream().map(Meaning::from).toList());
        }
    }

    @Schema(description = "뜻과 예문")
    public record Meaning(
            @Schema(description = "한국어 뜻", example = "겪다") String meaningKo,
            @Schema(description = "영어 예문 (fromSentence면 입력한 문장)") String exampleEn,
            @Schema(description = "예문 해석") String exampleKo,
            @Schema(description = "입력한 문장에서 쓰인 뜻인지") boolean fromSentence
    ) {
        static Meaning from(ExpressionSuggestion.Meaning m) {
            return new Meaning(m.meaningKo(), m.exampleEn(), m.exampleKo(), m.fromSentence());
        }
    }

    public static ExplainResponse from(AiExpressionService.Result result) {
        return new ExplainResponse(
                result.sentence(),
                result.suggestions().stream().map(Suggestion::from).toList(),
                result.cached(),
                result.remainingToday());
    }
}
