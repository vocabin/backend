package com.vocabin.api.listening;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "이어보기 위치 저장 요청")
public record SavePositionRequest(
        @Schema(description = "마지막으로 본 위치(초)", example = "223")
        @NotNull @Min(0) @Max(86_400) Integer seconds
) {}
