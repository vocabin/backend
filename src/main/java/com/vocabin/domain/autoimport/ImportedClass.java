package com.vocabin.domain.autoimport;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ImportedClass {

    private final Long id;
    private final Long memberId;
    private final String externalClassId;
    private final Long wordSetId;
    private final LocalDateTime importedAt;

    public static ImportedClass create(Long memberId, String externalClassId, Long wordSetId, LocalDateTime now) {
        return ImportedClass.builder()
                .memberId(memberId)
                .externalClassId(externalClassId)
                .wordSetId(wordSetId)
                .importedAt(now)
                .build();
    }
}
