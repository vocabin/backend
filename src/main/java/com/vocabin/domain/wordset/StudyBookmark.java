package com.vocabin.domain.wordset;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudyBookmark {

    private final Long id;
    private final Long memberId;
    private final Long wordSetId;
    private final int wordIndex;
    private final LocalDateTime updatedAt;

    public static StudyBookmark create(Long memberId, Long wordSetId, int wordIndex, ClockHolder clockHolder) {
        return StudyBookmark.builder()
                .memberId(memberId)
                .wordSetId(wordSetId)
                .wordIndex(wordIndex)
                .updatedAt(clockHolder.now())
                .build();
    }

    public StudyBookmark updateIndex(int wordIndex, ClockHolder clockHolder) {
        return StudyBookmark.builder()
                .id(this.id)
                .memberId(this.memberId)
                .wordSetId(this.wordSetId)
                .wordIndex(wordIndex)
                .updatedAt(clockHolder.now())
                .build();
    }
}
