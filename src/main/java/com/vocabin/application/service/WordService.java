package com.vocabin.application.service;

import com.vocabin.domain.word.Word;

import java.util.List;

public interface WordService {
    List<Word> getDueWords(Long memberId);
    List<Word> getAllShuffled(Long memberId);
    List<Word> getWordList(Long wordSetId);
    List<Word> getWeakWords(Long memberId, Long wordSetId);
    Word updateWord(Long wordId, String english, String korean);
    void deleteWord(Long wordId);
}
