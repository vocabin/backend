package com.vocabin.infrastructure.wordset;

import com.vocabin.domain.wordset.SavedWordSet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_word_set",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "word_set_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedWordSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "word_set_id", nullable = false)
    private Long wordSetId;

    @Column(name = "word_set_name", nullable = false, length = 100)
    private String wordSetName;

    @Column(nullable = false)
    private LocalDateTime savedAt;

    public static SavedWordSetEntity from(SavedWordSet domain) {
        SavedWordSetEntity entity = new SavedWordSetEntity();
        entity.id = domain.getId();
        entity.memberId = domain.getMemberId();
        entity.wordSetId = domain.getWordSetId();
        entity.wordSetName = domain.getWordSetName();
        entity.savedAt = domain.getSavedAt();
        return entity;
    }

    public SavedWordSet toModel() {
        return SavedWordSet.builder()
                .id(id)
                .memberId(memberId)
                .wordSetId(wordSetId)
                .wordSetName(wordSetName)
                .savedAt(savedAt)
                .build();
    }
}
