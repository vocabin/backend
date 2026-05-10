package com.vocabin.api.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학습 설정 수정 요청")
public record UpdateSettingsRequest(
        @Schema(description = "하루 목표 단어 수 (1~200)", example = "20")
        @NotNull @Min(1) @Max(200) Integer dailyGoal,

        @Schema(description = "랜덤 순서 여부", example = "true")
        @NotNull Boolean shuffle
) {}
