package com.vocabin.application.service;

import com.vocabin.application.port.out.ReviewScheduleRepository;
import com.vocabin.application.port.out.StudyRecordRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.domain.study.StudyMode;
import com.vocabin.domain.study.StudyRecord;
import com.vocabin.domain.word.Word;
import com.vocabin.domain.wordset.WordSet;
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
    private final WordSetRepository wordSetRepository;
    private final ClockHolder clockHolder;

    @Override
    @Transactional
    public StudyRecord record(Long memberId, Long wordId, StudyMode mode, boolean correct) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + wordId));
        WordSet wordSet = wordSetRepository.findById(word.getWordSetId())
                .orElseThrow(() -> new NoSuchElementException("단어 세트를 찾을 수 없습니다. id=" + word.getWordSetId()));
        if (!wordSet.getMemberId().equals(memberId)) {
            throw new SecurityException("해당 단어에 접근할 권한이 없습니다.");
        }

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
