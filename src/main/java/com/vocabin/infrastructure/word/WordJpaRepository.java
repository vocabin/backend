package com.vocabin.infrastructure.word;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordJpaRepository extends JpaRepository<WordEntity, Long> {

    @Query("SELECT w FROM WordEntity w WHERE w.wordSet.id = :wordSetId")
    List<WordEntity> findAllByWordSetId(@Param("wordSetId") Long wordSetId);
}
