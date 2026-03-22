package com.vocabin.application.port.out;

import com.vocabin.domain.wordset.WordSet;

import java.util.List;
import java.util.Optional;

public interface WordSetRepository {
    List<WordSet> findAll();
    Optional<WordSet> findById(Long id);
    WordSet save(WordSet wordSet);
}
