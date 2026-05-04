package com.vocabin.api.stats;

import com.vocabin.application.service.StatsService.StatsSummary;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "통계 요약 응답")
public record StatsSummaryResponse(
        @Schema(description = "총 단어 수", example = "120") long totalWords,
        @Schema(description = "전체 정답률 (0.0~1.0)", example = "0.72") double correctRate,
        @Schema(description = "연속 학습일", example = "5") int streakDays,
        @Schema(description = "총 학습 횟수", example = "340") long totalRecords
) {
    public static StatsSummaryResponse from(StatsSummary summary) {
        return new StatsSummaryResponse(
                summary.totalWords(),
                summary.correctRate(),
                summary.streakDays(),
                summary.totalRecords()
        );
    }
}
