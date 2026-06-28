package com.vocabin.application.service;

import com.vocabin.domain.wordset.StudyBookmark;

import java.util.Optional;

public interface StudyBookmarkService {
    Optional<StudyBookmark> getBookmark(Long memberId, Long wordSetId);
    StudyBookmark upsertBookmark(Long memberId, Long wordSetId, int wordIndex);
}
