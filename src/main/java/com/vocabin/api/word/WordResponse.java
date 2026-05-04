package com.vocabin.api.word;

import com.vocabin.domain.word.Word;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "단어 응답")
public record WordResponse(
        @Schema(description = "단어 ID") Long id,
        @Schema(description = "단어 세트 ID") Long wordSetId,
        @Schema(description = "영어") String english,
        @Schema(description = "한국어") String korean,
        @Schema(description = "생성 시간") LocalDateTime createdAt
) {
    public static WordResponse from(Word word) {
        return new WordResponse(
                word.getId(),
                word.getWordSetId(),
                word.getEnglish(),
                word.getKorean(),
                word.getCreatedAt()
        );
    }

    public static List<WordResponse> fromList(List<Word> words) {
        return words.stream().map(WordResponse::from).toList();
    }
}
