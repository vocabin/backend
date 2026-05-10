package com.vocabin.application.service;

import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.wordset.WordSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordSetServiceImpl implements WordSetService {

    private final WordSetRepository wordSetRepository;
    private final ClockHolder clockHolder;

    @Override
    public List<WordSet> getWordSets() {
        return wordSetRepository.findAll();
    }

    @Override
    @Transactional
    public WordSet createWordSet(String name) {
        WordSet wordSet = WordSet.create(name, clockHolder);
        return wordSetRepository.save(wordSet);
    }
}
