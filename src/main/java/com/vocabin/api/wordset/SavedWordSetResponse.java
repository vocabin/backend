package com.vocabin.api.wordset;

import com.vocabin.domain.wordset.SavedWordSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "저장된 단어 세트 응답")
public record SavedWordSetResponse(
        @Schema(description = "단어 세트 ID") Long wordSetId,
        @Schema(description = "단어 세트 이름") String name,
        @Schema(description = "저장 일시") LocalDateTime savedAt
) {
    public static SavedWordSetResponse from(SavedWordSet domain) {
        return new SavedWordSetResponse(domain.getWordSetId(), domain.getWordSetName(), domain.getSavedAt());
    }
}
