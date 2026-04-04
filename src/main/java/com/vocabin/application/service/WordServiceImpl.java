package com.vocabin.application.service;

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

    @Override
    public List<Word> getWordList(Long wordSetId) {
        wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new NoSuchElementException("단어 세트를 찾을 수 없습니다. id=" + wordSetId));
        return wordRepository.findAllByWordSetId(wordSetId);
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
