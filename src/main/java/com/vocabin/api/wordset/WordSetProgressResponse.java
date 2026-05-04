package com.vocabin.api.wordset;

import com.vocabin.domain.stats.WordSetProgress;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단어 세트별 진행률")
public record WordSetProgressResponse(
        @Schema(description = "단어 세트 ID", example = "1") Long wordSetId,
        @Schema(description = "세트 이름", example = "1주차") String name,
        @Schema(description = "총 단어 수", example = "30") int totalWords,
        @Schema(description = "학습한 단어 수", example = "20") int studiedWords
) {
    public static WordSetProgressResponse from(WordSetProgress progress) {
        return new WordSetProgressResponse(
                progress.wordSetId(),
                progress.name(),
                progress.totalWords(),
                progress.studiedWords()
        );
    }
}
