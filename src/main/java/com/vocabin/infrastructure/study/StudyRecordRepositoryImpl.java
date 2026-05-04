package com.vocabin.infrastructure.study;

import com.vocabin.application.port.out.StudyRecordRepository;
import com.vocabin.domain.study.StudyRecord;
import com.vocabin.infrastructure.word.WordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
@RequiredArgsConstructor
public class StudyRecordRepositoryImpl implements StudyRecordRepository {

    private final StudyRecordJpaRepository studyRecordJpaRepository;
    private final WordJpaRepository wordJpaRepository;

    @Override
    public List<Long> findWeakWordIds(Long wordSetId) {
        return studyRecordJpaRepository.findWeakWordIds(wordSetId);
    }

    @Override
    public StudyRecord save(StudyRecord studyRecord) {
        var wordEntity = wordJpaRepository.findById(studyRecord.getWordId())
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + studyRecord.getWordId()));
        return studyRecordJpaRepository.save(StudyRecordEntity.from(studyRecord, wordEntity)).toModel();
    }
}
