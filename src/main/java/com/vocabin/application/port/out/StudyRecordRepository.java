package com.vocabin.application.port.out;

import com.vocabin.domain.study.StudyRecord;

public interface StudyRecordRepository {
    StudyRecord save(StudyRecord studyRecord);
}
