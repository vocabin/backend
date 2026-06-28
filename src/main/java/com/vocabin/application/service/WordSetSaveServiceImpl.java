package com.vocabin.application.service;

import com.vocabin.application.port.out.SavedWordSetRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.wordset.SavedWordSet;
import com.vocabin.domain.wordset.WordSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordSetSaveServiceImpl implements WordSetSaveService {

    private final SavedWordSetRepository savedWordSetRepository;
    private final WordSetRepository wordSetRepository;
    private final ClockHolder clockHolder;

    @Override
    public List<SavedWordSet> getSavedWordSets(Long memberId) {
        return savedWordSetRepository.findAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public SavedWordSet saveWordSet(Long memberId, Long wordSetId) {
        return savedWordSetRepository.findByMemberIdAndWordSetId(memberId, wordSetId)
                .orElseGet(() -> {
                    WordSet wordSet = wordSetRepository.findById(wordSetId)
                            .orElseThrow(() -> new IllegalArgumentException("Word set not found: " + wordSetId));
                    SavedWordSet toSave = SavedWordSet.builder()
                            .memberId(memberId)
                            .wordSetId(wordSetId)
                            .wordSetName(wordSet.getName())
                            .savedAt(clockHolder.now())
                            .build();
                    return savedWordSetRepository.save(toSave);
                });
    }

    @Override
    @Transactional
    public void unsaveWordSet(Long memberId, Long wordSetId) {
        savedWordSetRepository.deleteByMemberIdAndWordSetId(memberId, wordSetId);
    }

    @Override
    public boolean isSaved(Long memberId, Long wordSetId) {
        return savedWordSetRepository.existsByMemberIdAndWordSetId(memberId, wordSetId);
    }
}
