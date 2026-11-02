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
            @Schema(description = "표현 (기본형)", example = "have been meaning to") String expression,
            @Schema(description = "한국어 뜻", example = "현재완료진행: ~하려고 계속 생각해 왔다") String meaningKo,
            @Schema(description = "영어 예문") String exampleEn,
            @Schema(description = "예문 해석") String exampleKo,
            @Schema(description = "종류: IDIOM, PHRASAL_VERB, EXPRESSION, GRAMMAR") SuggestionType type
    ) {
        static Suggestion from(ExpressionSuggestion s) {
            return new Suggestion(s.expression(), s.meaningKo(), s.exampleEn(), s.exampleKo(), s.type());
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
