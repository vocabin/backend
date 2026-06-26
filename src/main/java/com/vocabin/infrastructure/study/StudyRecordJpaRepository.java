package com.vocabin.infrastructure.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyRecordJpaRepository extends JpaRepository<StudyRecordEntity, Long> {

    @Query("""
            SELECT sr.word.id
            FROM StudyRecordEntity sr
            WHERE sr.correct = false
              AND sr.word.wordSet.memberId = :memberId
              AND (:wordSetId IS NULL OR sr.word.wordSet.id = :wordSetId)
            GROUP BY sr.word.id
            HAVING COUNT(sr) >= 3
            ORDER BY COUNT(sr) DESC
            """)
    List<Long> findWeakWordIds(@Param("memberId") Long memberId, @Param("wordSetId") Long wordSetId);
}
