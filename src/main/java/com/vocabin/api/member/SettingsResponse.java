package com.vocabin.api.member;

import com.vocabin.domain.member.MemberSettings;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학습 설정 응답")
public record SettingsResponse(
        @Schema(description = "하루 목표 단어 수", example = "20") int dailyGoal,
        @Schema(description = "랜덤 순서 여부", example = "true") boolean shuffle
) {
    public static SettingsResponse from(MemberSettings settings) {
        return new SettingsResponse(settings.getDailyGoal(), settings.isShuffle());
    }
}
