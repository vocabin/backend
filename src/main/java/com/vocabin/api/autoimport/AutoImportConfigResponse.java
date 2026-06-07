package com.vocabin.api.autoimport;

import com.vocabin.domain.autoimport.AutoImportConfig;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동 임포트 설정 응답")
public record AutoImportConfigResponse(
        @Schema(description = "요일 (1=월, 7=일)") int dayOfWeek,
        @Schema(description = "시간 (0-23)") int hour,
        @Schema(description = "활성화 여부") boolean enabled
) {
    public static AutoImportConfigResponse from(AutoImportConfig config) {
        return new AutoImportConfigResponse(config.getDayOfWeek(), config.getHour(), config.isEnabled());
    }
}
