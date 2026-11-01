package com.vocabin.application.service;

import com.vocabin.domain.word.Word;

import java.util.List;
import java.util.Optional;

public interface PocketService {
    Word add(Long memberId, String english, String korean, String memo);
    Optional<Word> addIfAbsent(Long memberId, String english, String korean, String memo);
    Long ensurePocketSetId(Long memberId);
    List<Word> getPocketWords(Long memberId);
    List<Word> getDueWords(Long memberId);
    void remove(Long memberId, Long wordId);
}
