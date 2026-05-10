package com.vocabin.api.stats;

import com.vocabin.domain.stats.DailyStudyStats;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "주간 학습 통계 응답")
public record StatsWeeklyResponse(
        @Schema(description = "요일별 학습 통계") List<DailyStatItem> days
) {
    @Schema(description = "일별 통계")
    public record DailyStatItem(
            @Schema(description = "날짜", example = "2026-05-04") LocalDate date,
            @Schema(description = "총 학습 횟수", example = "20") int total,
            @Schema(description = "정답 횟수", example = "15") int correct,
            @Schema(description = "정답률 (0.0~1.0)", example = "0.75") double correctRate
    ) {
        public static DailyStatItem from(DailyStudyStats stats) {
            return new DailyStatItem(stats.date(), stats.total(), stats.correct(), stats.correctRate());
        }
    }

    public static StatsWeeklyResponse from(List<DailyStudyStats> statsList) {
        return new StatsWeeklyResponse(statsList.stream().map(DailyStatItem::from).toList());
    }
}
