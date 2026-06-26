package com.vocabin.application.service;

import com.vocabin.domain.study.StudyMode;
import com.vocabin.domain.study.StudyRecord;

public interface StudyService {
    StudyRecord record(Long memberId, Long wordId, StudyMode mode, boolean correct);
}
