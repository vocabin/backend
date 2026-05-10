package com.vocabin.api.study;

import com.vocabin.domain.study.StudyMode;
import com.vocabin.domain.study.StudyRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "학습 결과 기록 응답")
public record StudyRecordResponse(
        @Schema(description = "기록 ID", example = "1")
        Long id,

        @Schema(description = "단어 ID", example = "1")
        Long wordId,

        @Schema(description = "학습 모드", example = "FLASHCARD")
        StudyMode mode,

        @Schema(description = "정답 여부", example = "true")
        boolean correct,

        @Schema(description = "학습 시각")
        LocalDateTime studiedAt
) {
    public static StudyRecordResponse from(StudyRecord studyRecord) {
        return new StudyRecordResponse(
                studyRecord.getId(),
                studyRecord.getWordId(),
                studyRecord.getMode(),
                studyRecord.isCorrect(),
                studyRecord.getStudiedAt()
        );
    }
}
