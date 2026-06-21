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
    public List<WordSet> getWordSets(Long memberId) {
        return wordSetRepository.findAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public WordSet createWordSet(String name, Long memberId) {
        WordSet wordSet = WordSet.create(name, memberId, clockHolder);
        return wordSetRepository.save(wordSet);
    }
}
