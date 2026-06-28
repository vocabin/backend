package com.vocabin.application.service;

import com.vocabin.domain.wordset.SavedWordSet;

import java.util.List;

public interface WordSetSaveService {
    List<SavedWordSet> getSavedWordSets(Long memberId);
    SavedWordSet saveWordSet(Long memberId, Long wordSetId);
    void unsaveWordSet(Long memberId, Long wordSetId);
    boolean isSaved(Long memberId, Long wordSetId);
}
