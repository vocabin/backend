package com.vocabin.api.wordset;

import com.vocabin.domain.wordset.WordSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "단어 세트 응답")
public record WordSetResponse(
        @Schema(description = "세트 ID") Long id,
        @Schema(description = "세트 이름") String name,
        @Schema(description = "생성 시간") LocalDateTime createdAt
) {
    public static WordSetResponse from(WordSet wordSet) {
        return new WordSetResponse(wordSet.getId(), wordSet.getName(), wordSet.getCreatedAt());
    }
}
