package com.vocabin.infrastructure.wordset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyBookmarkJpaRepository extends JpaRepository<StudyBookmarkEntity, Long> {
    Optional<StudyBookmarkEntity> findByMemberIdAndWordSetId(Long memberId, Long wordSetId);
}
