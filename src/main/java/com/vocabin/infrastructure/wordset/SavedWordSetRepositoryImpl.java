package com.vocabin.infrastructure.wordset;

import com.vocabin.application.port.out.SavedWordSetRepository;
import com.vocabin.domain.wordset.SavedWordSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SavedWordSetRepositoryImpl implements SavedWordSetRepository {

    private final SavedWordSetJpaRepository savedWordSetJpaRepository;

    @Override
    public List<SavedWordSet> findAllByMemberId(Long memberId) {
        return savedWordSetJpaRepository.findAllByMemberIdOrderBySavedAtDesc(memberId).stream()
                .map(SavedWordSetEntity::toModel)
                .toList();
    }

    @Override
    public Optional<SavedWordSet> findByMemberIdAndWordSetId(Long memberId, Long wordSetId) {
        return savedWordSetJpaRepository.findByMemberIdAndWordSetId(memberId, wordSetId)
                .map(SavedWordSetEntity::toModel);
    }

    @Override
    public SavedWordSet save(SavedWordSet savedWordSet) {
        return savedWordSetJpaRepository.save(SavedWordSetEntity.from(savedWordSet)).toModel();
    }

    @Override
    public void deleteByMemberIdAndWordSetId(Long memberId, Long wordSetId) {
        savedWordSetJpaRepository.deleteByMemberIdAndWordSetId(memberId, wordSetId);
    }

    @Override
    public boolean existsByMemberIdAndWordSetId(Long memberId, Long wordSetId) {
        return savedWordSetJpaRepository.existsByMemberIdAndWordSetId(memberId, wordSetId);
    }
}
