package com.vocabin.application.port.out;

import com.vocabin.domain.wordset.WordSet;
import com.vocabin.domain.wordset.WordSetType;

import java.util.List;
import java.util.Optional;

public interface WordSetRepository {
    List<WordSet> findAllByMemberIdAndType(Long memberId, WordSetType type);
    Optional<WordSet> findById(Long id);
    WordSet save(WordSet wordSet);
    void deleteById(Long id);
}
