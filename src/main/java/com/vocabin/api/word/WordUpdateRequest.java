package com.vocabin.api.word;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "단어 수정 요청")
public record WordUpdateRequest(
        @Schema(description = "영어")
        @NotBlank(message = "영어는 필수입니다.")
        String english,

        @Schema(description = "한국어")
        @NotBlank(message = "한국어는 필수입니다.")
        String korean
) {}
