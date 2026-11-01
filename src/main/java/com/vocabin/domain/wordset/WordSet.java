package com.vocabin.domain.wordset;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WordSet {

    private static final String POCKET_NAME = "포켓";

    private final Long id;
    private final Long memberId;
    private final String name;
    private final WordSetType type;
    private final LocalDateTime createdAt;

    public static WordSet create(String name, Long memberId, ClockHolder clockHolder) {
        return WordSet.builder()
                .memberId(memberId)
                .name(name)
                .type(WordSetType.REGULAR)
                .createdAt(clockHolder.now())
                .build();
    }

    public static WordSet createPocket(Long memberId, ClockHolder clockHolder) {
        return WordSet.builder()
                .memberId(memberId)
                .name(POCKET_NAME)
                .type(WordSetType.POCKET)
                .createdAt(clockHolder.now())
                .build();
    }

    public WordSet rename(String name) {
        return WordSet.builder()
                .id(this.id)
                .memberId(this.memberId)
                .name(name)
                .type(this.type)
                .createdAt(this.createdAt)
                .build();
    }

    public boolean isPocket() {
        return type == WordSetType.POCKET;
    }
}
