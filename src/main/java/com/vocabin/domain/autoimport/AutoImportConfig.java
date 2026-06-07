package com.vocabin.domain.autoimport;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoImportConfig {

    private final Long id;
    private final Long memberId;
    private final int dayOfWeek; // 1=Mon, 7=Sun
    private final int hour;      // 0–23
    private final boolean enabled;

    public static AutoImportConfig defaultConfig(Long memberId) {
        return AutoImportConfig.builder()
                .memberId(memberId)
                .dayOfWeek(3)   // Wednesday
                .hour(21)
                .enabled(true)
                .build();
    }

    public AutoImportConfig update(int dayOfWeek, int hour, boolean enabled) {
        return AutoImportConfig.builder()
                .id(this.id)
                .memberId(this.memberId)
                .dayOfWeek(dayOfWeek)
                .hour(hour)
                .enabled(enabled)
                .build();
    }
}
