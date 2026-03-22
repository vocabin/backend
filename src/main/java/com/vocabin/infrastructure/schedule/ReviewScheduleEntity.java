package com.vocabin.infrastructure.schedule;

import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.infrastructure.word.WordEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false, unique = true)
    private WordEntity word;

    @Column(nullable = false)
    private LocalDate nextReviewAt;

    @Column(nullable = false)
    private int intervalDays;

    @Column(nullable = false)
    private float easeFactor;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static ReviewScheduleEntity from(ReviewSchedule reviewSchedule, WordEntity wordEntity) {
        ReviewScheduleEntity entity = new ReviewScheduleEntity();
        entity.id = reviewSchedule.getId();
        entity.word = wordEntity;
        entity.nextReviewAt = reviewSchedule.getNextReviewAt();
        entity.intervalDays = reviewSchedule.getIntervalDays();
        entity.easeFactor = reviewSchedule.getEaseFactor();
        entity.updatedAt = reviewSchedule.getUpdatedAt();
        return entity;
    }

    public ReviewSchedule toModel() {
        return ReviewSchedule.builder()
                .id(id)
                .wordId(word.getId())
                .nextReviewAt(nextReviewAt)
                .intervalDays(intervalDays)
                .easeFactor(easeFactor)
                .updatedAt(updatedAt)
                .build();
    }
}
