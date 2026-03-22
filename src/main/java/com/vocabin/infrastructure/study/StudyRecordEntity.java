package com.vocabin.infrastructure.study;

import com.vocabin.domain.study.StudyMode;
import com.vocabin.domain.study.StudyRecord;
import com.vocabin.infrastructure.word.WordEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private WordEntity word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StudyMode mode;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(nullable = false, updatable = false)
    private LocalDateTime studiedAt;

    public static StudyRecordEntity from(StudyRecord studyRecord, WordEntity wordEntity) {
        StudyRecordEntity entity = new StudyRecordEntity();
        entity.id = studyRecord.getId();
        entity.word = wordEntity;
        entity.mode = studyRecord.getMode();
        entity.correct = studyRecord.isCorrect();
        entity.studiedAt = studyRecord.getStudiedAt();
        return entity;
    }

    public StudyRecord toModel() {
        return StudyRecord.builder()
                .id(id)
                .wordId(word.getId())
                .mode(mode)
                .correct(correct)
                .studiedAt(studiedAt)
                .build();
    }
}
