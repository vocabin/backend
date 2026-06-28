package com.vocabin.api.wordset;

import com.vocabin.domain.wordset.StudyBookmark;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "학습 책갈피 응답")
public record StudyBookmarkResponse(
        @Schema(description = "단어 세트 ID") Long wordSetId,
        @Schema(description = "책갈피 단어 인덱스 (0-based)") int wordIndex,
        @Schema(description = "마지막 업데이트 일시") LocalDateTime updatedAt
) {
    public static StudyBookmarkResponse from(StudyBookmark domain) {
        return new StudyBookmarkResponse(domain.getWordSetId(), domain.getWordIndex(), domain.getUpdatedAt());
    }
}
