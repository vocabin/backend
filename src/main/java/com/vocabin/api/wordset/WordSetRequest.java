package com.vocabin.api.wordset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "단어 세트 생성 요청")
public record WordSetRequest(
        @Schema(description = "세트 이름 (예: 1주차)")
        @NotBlank(message = "세트 이름은 필수입니다.")
        String name
) {
}
