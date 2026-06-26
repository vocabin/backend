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
    public long countTotalWords(Long memberId) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM word w
                JOIN word_set ws ON ws.id = w.word_set_id
                WHERE ws.member_id = :memberId
                """)
                .setParameter("memberId", memberId)
                .getSingleResult()).longValue();
    }

    @Override
    public long countTotalRecords(Long memberId) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM study_record sr
                JOIN word w ON w.id = sr.word_id
                JOIN word_set ws ON ws.id = w.word_set_id
                WHERE ws.member_id = :memberId
                """)
                .setParameter("memberId", memberId)
                .getSingleResult()).longValue();
    }

    @Override
    public long countCorrectRecords(Long memberId) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM study_record sr
                JOIN word w ON w.id = sr.word_id
                JOIN word_set ws ON ws.id = w.word_set_id
                WHERE ws.member_id = :memberId AND sr.is_correct = 1
                """)
                .setParameter("memberId", memberId)
                .getSingleResult()).longValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LocalDate> findDistinctStudyDates(Long memberId) {
        List<Date> dates = em.createNativeQuery("""
                SELECT DISTINCT DATE(sr.studied_at)
                FROM study_record sr
                JOIN word w ON w.id = sr.word_id
                JOIN word_set ws ON ws.id = w.word_set_id
                WHERE ws.member_id = :memberId
                ORDER BY DATE(sr.studied_at) DESC
                """)
                .setParameter("memberId", memberId)
                .getResultList();
        return dates.stream().map(Date::toLocalDate).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DailyStudyStats> findDailyStudyStats(LocalDate from, LocalDate to, Long memberId) {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT DATE(sr.studied_at),
                       COUNT(*),
                       SUM(CASE WHEN sr.is_correct = 1 THEN 1 ELSE 0 END)
                FROM study_record sr
                JOIN word w ON w.id = sr.word_id
                JOIN word_set ws ON ws.id = w.word_set_id
                WHERE ws.member_id = :memberId
                  AND DATE(sr.studied_at) BETWEEN :from AND :to
                GROUP BY DATE(sr.studied_at)
                ORDER BY DATE(sr.studied_at)
                """)
                .setParameter("memberId", memberId)
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
    public List<LocalDate> findStudyDatesInMonth(int year, int month, Long memberId) {
        List<Date> dates = em.createNativeQuery("""
                SELECT DISTINCT DATE(sr.studied_at)
                FROM study_record sr
                JOIN word w ON w.id = sr.word_id
                JOIN word_set ws ON ws.id = w.word_set_id
                WHERE ws.member_id = :memberId
                  AND YEAR(sr.studied_at) = :year
                  AND MONTH(sr.studied_at) = :month
                ORDER BY DATE(sr.studied_at)
                """)
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
        return dates.stream().map(Date::toLocalDate).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WordSetProgress> findWordSetProgress(Long memberId) {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT ws.id, ws.name,
                       COUNT(DISTINCT w.id),
                       COUNT(DISTINCT sr.word_id)
                FROM word_set ws
                LEFT JOIN word w ON w.word_set_id = ws.id
                LEFT JOIN study_record sr ON sr.word_id = w.id
                WHERE ws.member_id = :memberId
                GROUP BY ws.id, ws.name
                ORDER BY ws.id
                """)
                .setParameter("memberId", memberId)
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
