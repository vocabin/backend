package com.vocabin.infrastructure.ai;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_explain_log", indexes = {
        @Index(name = "idx_ai_explain_member_hash", columnList = "member_id, sentence_hash"),
        @Index(name = "idx_ai_explain_member_created", columnList = "member_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiExplainLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "sentence_hash", nullable = false, length = 64)
    private String sentenceHash;

    @Column(nullable = false, length = 300)
    private String sentence;

    @Column(name = "suggestions_json", nullable = false, columnDefinition = "TEXT")
    private String suggestionsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    static AiExplainLogEntity of(Long id, Long memberId, String sentenceHash, String sentence,
                                 String suggestionsJson, LocalDateTime createdAt) {
        AiExplainLogEntity entity = new AiExplainLogEntity();
        entity.id = id;
        entity.memberId = memberId;
        entity.sentenceHash = sentenceHash;
        entity.sentence = sentence;
        entity.suggestionsJson = suggestionsJson;
        entity.createdAt = createdAt;
        return entity;
    }
}
