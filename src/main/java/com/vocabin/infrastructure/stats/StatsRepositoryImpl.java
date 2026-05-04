package com.vocabin.infrastructure.stats;

import com.vocabin.application.port.out.StatsRepository;
import com.vocabin.domain.stats.DailyStudyStats;
import com.vocabin.domain.stats.WordSetProgress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class StatsRepositoryImpl implements StatsRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public long countTotalWords() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM word").getSingleResult()).longValue();
    }

    @Override
    public long countTotalRecords() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM study_record").getSingleResult()).longValue();
    }

    @Override
    public long countCorrectRecords() {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM study_record WHERE is_correct = 1").getSingleResult()).longValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LocalDate> findDistinctStudyDates() {
        List<Date> dates = em.createNativeQuery(
                "SELECT DISTINCT DATE(studied_at) FROM study_record ORDER BY DATE(studied_at) DESC"
        ).getResultList();
        return dates.stream().map(Date::toLocalDate).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DailyStudyStats> findDailyStudyStats(LocalDate from, LocalDate to) {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT DATE(studied_at),
                       COUNT(*),
                       SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END)
                FROM study_record
                WHERE DATE(studied_at) BETWEEN :from AND :to
                GROUP BY DATE(studied_at)
                ORDER BY DATE(studied_at)
                """)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return rows.stream()
                .map(row -> new DailyStudyStats(
                        ((Date) row[0]).toLocalDate(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).intValue()
                ))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LocalDate> findStudyDatesInMonth(int year, int month) {
        List<Date> dates = em.createNativeQuery("""
                SELECT DISTINCT DATE(studied_at)
                FROM study_record
                WHERE YEAR(studied_at) = :year AND MONTH(studied_at) = :month
                ORDER BY DATE(studied_at)
                """)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
        return dates.stream().map(Date::toLocalDate).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WordSetProgress> findWordSetProgress() {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT ws.id, ws.name,
                       COUNT(DISTINCT w.id),
                       COUNT(DISTINCT sr.word_id)
                FROM word_set ws
                LEFT JOIN word w ON w.word_set_id = ws.id
                LEFT JOIN study_record sr ON sr.word_id = w.id
                GROUP BY ws.id, ws.name
                ORDER BY ws.id
                """)
                .getResultList();

        return rows.stream()
                .map(row -> new WordSetProgress(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).intValue(),
                        ((Number) row[3]).intValue()
                ))
                .toList();
    }
}
