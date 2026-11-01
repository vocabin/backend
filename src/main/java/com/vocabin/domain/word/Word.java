package com.vocabin.domain.word;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Locale;

@Getter
@Builder
public class Word {

    private final Long id;
    private final Long wordSetId;
    private final String english;
    private final String korean;
    private final String memo;
    private final LocalDateTime createdAt;

    public static Word create(Long wordSetId, String english, String korean, ClockHolder clockHolder) {
        return Word.builder()
                .wordSetId(wordSetId)
                .english(english)
                .korean(korean)
                .createdAt(clockHolder.now())
                .build();
    }

    public static Word createPocketItem(Long pocketSetId, String english, String korean, String memo, ClockHolder clockHolder) {
        return Word.builder()
                .wordSetId(pocketSetId)
                .english(english.trim())
                .korean(korean.trim())
                .memo(memo == null || memo.isBlank() ? null : memo.trim())
                .createdAt(clockHolder.now())
                .build();
    }

    public Word update(String english, String korean) {
        return Word.builder()
                .id(this.id)
                .wordSetId(this.wordSetId)
                .english(english)
                .korean(korean)
                .memo(this.memo)
                .createdAt(this.createdAt)
                .build();
    }

    // Ignores case and extra whitespace so "Take  care of" equals "take care of"
    public boolean isSameEnglish(String other) {
        return normalize(this.english).equals(normalize(other));
    }

    private static String normalize(String text) {
        return text.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
