package com.vocabin.domain.word;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Word {

    private final Long id;
    private final Long wordSetId;
    private final String english;
    private final String korean;
    private final LocalDateTime createdAt;

    public static Word create(Long wordSetId, String english, String korean, ClockHolder clockHolder) {
        return Word.builder()
                .wordSetId(wordSetId)
                .english(english)
                .korean(korean)
                .createdAt(clockHolder.now())
                .build();
    }

    public Word update(String english, String korean) {
        return Word.builder()
                .id(this.id)
                .wordSetId(this.wordSetId)
                .english(english)
                .korean(korean)
                .createdAt(this.createdAt)
                .build();
    }
}
