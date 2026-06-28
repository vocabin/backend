package com.vocabin.api.wordset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "책갈피 업데이트 요청")
public record UpdateBookmarkRequest(
        @Schema(description = "책갈피 단어 인덱스 (0-based)") @Min(0) int wordIndex
) {}
