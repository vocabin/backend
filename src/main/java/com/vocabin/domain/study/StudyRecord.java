package com.vocabin.domain.study;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudyRecord {

    private final Long id;
    private final Long wordId;
    private final StudyMode mode;
    private final boolean correct;
    private final LocalDateTime studiedAt;

    public static StudyRecord create(Long wordId, StudyMode mode, boolean correct, ClockHolder clockHolder) {
        return StudyRecord.builder()
                .wordId(wordId)
                .mode(mode)
                .correct(correct)
                .studiedAt(clockHolder.now())
                .build();
    }
}
