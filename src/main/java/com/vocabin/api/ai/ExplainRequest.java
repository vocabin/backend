package com.vocabin.api.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 표현 추출 요청")
public record ExplainRequest(
        @Schema(description = "듣다가 이해하지 못한 영어 문장 하나 (최대 300자)", example = "I've been meaning to tell you about it.")
        @NotBlank @Size(max = 300) String sentence
) {}
