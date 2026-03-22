package com.vocabin.infrastructure.wordset;

import com.vocabin.domain.wordset.WordSet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "word_set")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static WordSetEntity from(WordSet wordSet) {
        WordSetEntity entity = new WordSetEntity();
        entity.id = wordSet.getId();
        entity.name = wordSet.getName();
        entity.createdAt = wordSet.getCreatedAt();
        return entity;
    }

    public WordSet toModel() {
        return WordSet.builder()
                .id(id)
                .name(name)
                .createdAt(createdAt)
                .build();
    }
}
