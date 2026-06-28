package com.vocabin.infrastructure.wordset;

import com.vocabin.application.port.out.StudyBookmarkRepository;
import com.vocabin.domain.wordset.StudyBookmark;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudyBookmarkRepositoryImpl implements StudyBookmarkRepository {

    private final StudyBookmarkJpaRepository studyBookmarkJpaRepository;

    @Override
    public Optional<StudyBookmark> findByMemberIdAndWordSetId(Long memberId, Long wordSetId) {
        return studyBookmarkJpaRepository.findByMemberIdAndWordSetId(memberId, wordSetId)
                .map(StudyBookmarkEntity::toModel);
    }

    @Override
    public StudyBookmark save(StudyBookmark bookmark) {
        return studyBookmarkJpaRepository.save(StudyBookmarkEntity.from(bookmark)).toModel();
    }
}
