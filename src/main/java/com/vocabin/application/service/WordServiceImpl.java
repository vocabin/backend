package com.vocabin.application.service;

import com.vocabin.application.port.out.StudyRecordRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.domain.word.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final WordSetRepository wordSetRepository;
    private final StudyRecordRepository studyRecordRepository;

    @Override
    public List<Word> getWordList(Long wordSetId) {
        wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new NoSuchElementException("단어 세트를 찾을 수 없습니다. id=" + wordSetId));
        return wordRepository.findAllByWordSetId(wordSetId);
    }

    @Override
    public List<Word> getWeakWords(Long wordSetId) {
        List<Long> wordIds = studyRecordRepository.findWeakWordIds(wordSetId);
        if (wordIds.isEmpty()) {
            return List.of();
        }
        // wordIds는 오답 수 내림차순으로 정렬된 상태 — 조회 후 순서 복원
        List<Word> words = wordRepository.findAllByIds(wordIds);
        return wordIds.stream()
                .map(id -> words.stream().filter(w -> w.getId().equals(id)).findFirst().orElseThrow())
                .toList();
    }

    @Override
    @Transactional
    public Word updateWord(Long wordId, String english, String korean) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + wordId));
        return wordRepository.save(word.update(english, korean));
    }

    @Override
    @Transactional
    public void deleteWord(Long wordId) {
        wordRepository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + wordId));
        wordRepository.deleteById(wordId);
    }
}
