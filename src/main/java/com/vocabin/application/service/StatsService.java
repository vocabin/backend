package com.vocabin.application.service;

import com.vocabin.domain.stats.DailyStudyStats;
import com.vocabin.domain.stats.WordSetProgress;

import java.util.List;

public interface StatsService {
    StatsSummary getSummary();
    List<DailyStudyStats> getWeeklyStats();
    List<java.time.LocalDate> getCalendar(int year, int month);
    List<WordSetProgress> getWordSetProgress();

    record StatsSummary(long totalWords, double correctRate, int streakDays, long totalRecords) {}
}
