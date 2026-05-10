package com.vocabin.infrastructure.wordset;

import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.domain.wordset.WordSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WordSetRepositoryImpl implements WordSetRepository {

    private final WordSetJpaRepository wordSetJpaRepository;

    @Override
    public List<WordSet> findAll() {
        return wordSetJpaRepository.findAll().stream()
                .map(WordSetEntity::toModel)
                .toList();
    }

    @Override
    public Optional<WordSet> findById(Long id) {
        return wordSetJpaRepository.findById(id)
                .map(WordSetEntity::toModel);
    }

    @Override
    public WordSet save(WordSet wordSet) {
        return wordSetJpaRepository.save(WordSetEntity.from(wordSet)).toModel();
    }
}
