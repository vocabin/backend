package com.vocabin.api.stats;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "월별 학습 히트맵 응답")
public record StatsCalendarResponse(
        @Schema(description = "학습한 날짜 목록") List<LocalDate> studiedDates
) {
    public static StatsCalendarResponse from(List<LocalDate> dates) {
        return new StatsCalendarResponse(dates);
    }
}
