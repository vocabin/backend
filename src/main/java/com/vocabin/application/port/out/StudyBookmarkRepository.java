package com.vocabin.application.port.out;

import com.vocabin.domain.wordset.StudyBookmark;

import java.util.Optional;

public interface StudyBookmarkRepository {
    Optional<StudyBookmark> findByMemberIdAndWordSetId(Long memberId, Long wordSetId);
    StudyBookmark save(StudyBookmark bookmark);
}
