package com.vocabin.application.port.out;

import com.vocabin.domain.wordset.SavedWordSet;

import java.util.List;
import java.util.Optional;

public interface SavedWordSetRepository {
    List<SavedWordSet> findAllByMemberId(Long memberId);
    Optional<SavedWordSet> findByMemberIdAndWordSetId(Long memberId, Long wordSetId);
    SavedWordSet save(SavedWordSet savedWordSet);
    void deleteByMemberIdAndWordSetId(Long memberId, Long wordSetId);
    boolean existsByMemberIdAndWordSetId(Long memberId, Long wordSetId);
}
