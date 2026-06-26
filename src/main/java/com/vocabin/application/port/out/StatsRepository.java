package com.vocabin.application.port.out;

import com.vocabin.domain.stats.DailyStudyStats;
import com.vocabin.domain.stats.WordSetProgress;

import java.time.LocalDate;
import java.util.List;

public interface StatsRepository {
    long countTotalWords(Long memberId);
    long countTotalRecords(Long memberId);
    long countCorrectRecords(Long memberId);
    List<LocalDate> findDistinctStudyDates(Long memberId);
    List<DailyStudyStats> findDailyStudyStats(LocalDate from, LocalDate to, Long memberId);
    List<LocalDate> findStudyDatesInMonth(int year, int month, Long memberId);
    List<WordSetProgress> findWordSetProgress(Long memberId);
}
