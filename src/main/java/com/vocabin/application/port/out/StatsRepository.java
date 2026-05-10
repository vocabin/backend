package com.vocabin.application.port.out;

import com.vocabin.domain.stats.DailyStudyStats;
import com.vocabin.domain.stats.WordSetProgress;

import java.time.LocalDate;
import java.util.List;

public interface StatsRepository {
    long countTotalWords();
    long countTotalRecords();
    long countCorrectRecords();
    List<LocalDate> findDistinctStudyDates();
    List<DailyStudyStats> findDailyStudyStats(LocalDate from, LocalDate to);
    List<LocalDate> findStudyDatesInMonth(int year, int month);
    List<WordSetProgress> findWordSetProgress();
}
