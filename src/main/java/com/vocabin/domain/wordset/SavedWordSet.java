package com.vocabin.domain.wordset;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SavedWordSet {

    private final Long id;
    private final Long memberId;
    private final Long wordSetId;
    private final String wordSetName;
    private final LocalDateTime savedAt;

    public static SavedWordSet create(Long memberId, Long wordSetId, ClockHolder clockHolder) {
        return SavedWordSet.builder()
                .memberId(memberId)
                .wordSetId(wordSetId)
                .savedAt(clockHolder.now())
                .build();
    }
}
