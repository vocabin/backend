package com.vocabin.application.service;

import com.vocabin.application.port.out.StudyBookmarkRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.wordset.StudyBookmark;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyBookmarkServiceImpl implements StudyBookmarkService {

    private final StudyBookmarkRepository studyBookmarkRepository;
    private final ClockHolder clockHolder;

    @Override
    public Optional<StudyBookmark> getBookmark(Long memberId, Long wordSetId) {
        return studyBookmarkRepository.findByMemberIdAndWordSetId(memberId, wordSetId);
    }

    @Override
    @Transactional
    public StudyBookmark upsertBookmark(Long memberId, Long wordSetId, int wordIndex) {
        StudyBookmark bookmark = studyBookmarkRepository
                .findByMemberIdAndWordSetId(memberId, wordSetId)
                .map(existing -> existing.updateIndex(wordIndex, clockHolder))
                .orElseGet(() -> StudyBookmark.create(memberId, wordSetId, wordIndex, clockHolder));
        return studyBookmarkRepository.save(bookmark);
    }
}
