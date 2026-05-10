package com.vocabin.application.service;

import com.vocabin.application.port.out.StatsRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.stats.DailyStudyStats;
import com.vocabin.domain.stats.WordSetProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final ClockHolder clockHolder;

    @Override
    public StatsSummary getSummary() {
        long totalWords = statsRepository.countTotalWords();
        long totalRecords = statsRepository.countTotalRecords();
        long correctRecords = statsRepository.countCorrectRecords();
        double correctRate = totalRecords == 0 ? 0.0 : (double) correctRecords / totalRecords;

        List<LocalDate> studyDates = statsRepository.findDistinctStudyDates();
        int streakDays = calculateStreak(studyDates, clockHolder.today());

        return new StatsSummary(totalWords, correctRate, streakDays, totalRecords);
    }

    @Override
    public List<DailyStudyStats> getWeeklyStats() {
        LocalDate today = clockHolder.today();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        return statsRepository.findDailyStudyStats(monday, today);
    }

    @Override
    public List<LocalDate> getCalendar(int year, int month) {
        return statsRepository.findStudyDatesInMonth(year, month);
    }

    @Override
    public List<WordSetProgress> getWordSetProgress() {
        return statsRepository.findWordSetProgress();
    }

    private int calculateStreak(List<LocalDate> sortedDescDates, LocalDate today) {
        if (sortedDescDates.isEmpty()) return 0;

        LocalDate mostRecent = sortedDescDates.get(0);
        if (!mostRecent.equals(today) && !mostRecent.equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate expected = mostRecent;
        for (LocalDate date : sortedDescDates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
