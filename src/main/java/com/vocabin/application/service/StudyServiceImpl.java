package com.vocabin.application.service;

import com.vocabin.application.port.out.ReviewScheduleRepository;
import com.vocabin.application.port.out.StudyRecordRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.domain.study.StudyMode;
import com.vocabin.domain.study.StudyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final StudyRecordRepository studyRecordRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final WordRepository wordRepository;
    private final ClockHolder clockHolder;

    @Override
    @Transactional
    public StudyRecord record(Long wordId, StudyMode mode, boolean correct) {
        wordRepository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + wordId));

        StudyRecord studyRecord = StudyRecord.create(wordId, mode, correct, clockHolder);
        studyRecordRepository.save(studyRecord);

        ReviewSchedule schedule = reviewScheduleRepository.findByWordId(wordId)
                .orElse(ReviewSchedule.initialize(wordId, clockHolder));

        ReviewSchedule updated = correct
                ? schedule.recordCorrect(clockHolder)
                : schedule.recordIncorrect(clockHolder);

        reviewScheduleRepository.save(updated);

        return studyRecord;
    }
}
