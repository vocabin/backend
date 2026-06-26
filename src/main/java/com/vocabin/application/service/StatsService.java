package com.vocabin.application.service;

import com.vocabin.domain.stats.DailyStudyStats;
import com.vocabin.domain.stats.WordSetProgress;

import java.util.List;

public interface StatsService {
    StatsSummary getSummary(Long memberId);
    List<DailyStudyStats> getWeeklyStats(Long memberId);
    List<java.time.LocalDate> getCalendar(int year, int month, Long memberId);
    List<WordSetProgress> getWordSetProgress(Long memberId);

    record StatsSummary(long totalWords, double correctRate, int streakDays, long totalRecords) {}
}
