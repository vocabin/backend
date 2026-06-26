package com.vocabin.application.port.out;

import com.vocabin.domain.study.StudyRecord;

import java.util.List;

public interface StudyRecordRepository {
    StudyRecord save(StudyRecord studyRecord);
    List<Long> findWeakWordIds(Long memberId, Long wordSetId);
}
