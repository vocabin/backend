package com.vocabin.api.autoimport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "자동 임포트 설정 수정 요청")
public record UpdateAutoImportConfigRequest(
        @Schema(description = "요일 (1=월, 7=일)") @Min(1) @Max(7) int dayOfWeek,
        @Schema(description = "시간 (0-23)") @Min(0) @Max(23) int hour,
        @Schema(description = "활성화 여부") boolean enabled
) {}
