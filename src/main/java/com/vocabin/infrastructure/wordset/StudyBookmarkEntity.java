package com.vocabin.infrastructure.wordset;

import com.vocabin.domain.wordset.StudyBookmark;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_bookmark",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "word_set_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyBookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "word_set_id", nullable = false)
    private Long wordSetId;

    @Column(name = "word_index", nullable = false)
    private int wordIndex;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static StudyBookmarkEntity from(StudyBookmark domain) {
        StudyBookmarkEntity entity = new StudyBookmarkEntity();
        entity.id = domain.getId();
        entity.memberId = domain.getMemberId();
        entity.wordSetId = domain.getWordSetId();
        entity.wordIndex = domain.getWordIndex();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public StudyBookmark toModel() {
        return StudyBookmark.builder()
                .id(id)
                .memberId(memberId)
                .wordSetId(wordSetId)
                .wordIndex(wordIndex)
                .updatedAt(updatedAt)
                .build();
    }
}
