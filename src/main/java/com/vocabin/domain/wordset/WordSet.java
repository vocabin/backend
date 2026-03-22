package com.vocabin.domain.wordset;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WordSet {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public static WordSet create(String name, ClockHolder clockHolder) {
        return WordSet.builder()
                .name(name)
                .createdAt(clockHolder.now())
                .build();
    }

    public WordSet rename(String name) {
        return WordSet.builder()
                .id(this.id)
                .name(name)
                .createdAt(this.createdAt)
                .build();
    }
}
