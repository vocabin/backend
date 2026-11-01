package com.vocabin.mock;

import com.vocabin.application.port.out.StudyRecordRepository;
import com.vocabin.domain.study.StudyRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FakeStudyRecordRepository implements StudyRecordRepository {

    private final List<StudyRecord> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public StudyRecord save(StudyRecord studyRecord) {
        StudyRecord saved = StudyRecord.builder()
                .id(idGenerator.incrementAndGet())
                .wordId(studyRecord.getWordId())
                .mode(studyRecord.getMode())
                .correct(studyRecord.isCorrect())
                .studiedAt(studyRecord.getStudiedAt())
                .build();
        store.add(saved);
        return saved;
    }

    @Override
    public List<Long> findWeakWordIds(Long memberId, Long wordSetId) {
        return List.of();
    }
}
