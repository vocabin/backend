package com.vocabin.infrastructure.word;

import com.vocabin.domain.word.Word;
import com.vocabin.infrastructure.wordset.WordSetEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "word")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_set_id", nullable = false)
    private WordSetEntity wordSet;

    @Column(nullable = false, length = 500)
    private String english;

    @Column(nullable = false, length = 500)
    private String korean;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static WordEntity from(Word word, WordSetEntity wordSetEntity) {
        WordEntity entity = new WordEntity();
        entity.id = word.getId();
        entity.wordSet = wordSetEntity;
        entity.english = word.getEnglish();
        entity.korean = word.getKorean();
        entity.createdAt = word.getCreatedAt();
        return entity;
    }

    public Word toModel() {
        return Word.builder()
                .id(id)
                .wordSetId(wordSet.getId())
                .english(english)
                .korean(korean)
                .createdAt(createdAt)
                .build();
    }
}
