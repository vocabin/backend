package com.vocabin.infrastructure.wordset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedWordSetJpaRepository extends JpaRepository<SavedWordSetEntity, Long> {
    List<SavedWordSetEntity> findAllByMemberIdOrderBySavedAtDesc(Long memberId);
    Optional<SavedWordSetEntity> findByMemberIdAndWordSetId(Long memberId, Long wordSetId);
    void deleteByMemberIdAndWordSetId(Long memberId, Long wordSetId);
    boolean existsByMemberIdAndWordSetId(Long memberId, Long wordSetId);
}
