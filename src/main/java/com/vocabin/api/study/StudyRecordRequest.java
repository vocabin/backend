package com.vocabin.api.study;

import com.vocabin.domain.study.StudyMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학습 결과 기록 요청")
public record StudyRecordRequest(
        @Schema(description = "단어 ID", example = "1")
        @NotNull Long wordId,

        @Schema(description = "학습 모드", example = "FLASHCARD")
        @NotNull StudyMode mode,

        @Schema(description = "정답 여부", example = "true")
        @NotNull Boolean correct
) {}
