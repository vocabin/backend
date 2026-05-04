package com.vocabin.api.stats;

import com.vocabin.application.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stats", description = "학습 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "통계 요약", description = "총 단어 수 / 전체 정답률 / 연속 학습일 / 총 학습 횟수를 반환합니다.")
    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryResponse> getSummary() {
        return ResponseEntity.ok(StatsSummaryResponse.from(statsService.getSummary()));
    }

    @Operation(summary = "주간 학습 통계", description = "이번 주 월요일부터 오늘까지 요일별 정답률을 반환합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<StatsWeeklyResponse> getWeekly() {
        return ResponseEntity.ok(StatsWeeklyResponse.from(statsService.getWeeklyStats()));
    }

    @Operation(summary = "월별 학습 히트맵", description = "해당 연월에 학습한 날짜 목록을 반환합니다.")
    @GetMapping("/calendar")
    public ResponseEntity<StatsCalendarResponse> getCalendar(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(StatsCalendarResponse.from(statsService.getCalendar(year, month)));
    }
}
