package com.vocabin.domain.schedule;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewSchedule {

    private static final float MIN_EASE_FACTOR = 1.3f;
    private static final float DEFAULT_EASE_FACTOR = 2.5f;

    private final Long id;
    private final Long wordId;
    private final LocalDate nextReviewAt;
    private final int intervalDays;
    private final float easeFactor;
    private final LocalDateTime updatedAt;

    public static ReviewSchedule initialize(Long wordId, ClockHolder clockHolder) {
        return ReviewSchedule.builder()
                .wordId(wordId)
                .intervalDays(1)
                .easeFactor(DEFAULT_EASE_FACTOR)
                .nextReviewAt(clockHolder.today().plusDays(1))
                .updatedAt(clockHolder.now())
                .build();
    }

    // SM-2: 정답 시 interval 늘림, 새 객체 반환
    public ReviewSchedule recordCorrect(ClockHolder clockHolder) {
        int newIntervalDays = (int) Math.ceil(this.intervalDays * this.easeFactor);
        return ReviewSchedule.builder()
                .id(this.id)
                .wordId(this.wordId)
                .intervalDays(newIntervalDays)
                .easeFactor(this.easeFactor)
                .nextReviewAt(clockHolder.today().plusDays(newIntervalDays))
                .updatedAt(clockHolder.now())
                .build();
    }

    // SM-2: 오답 시 interval 초기화, ease_factor 감소 (최소 1.3), 새 객체 반환
    public ReviewSchedule recordIncorrect(ClockHolder clockHolder) {
        float newEaseFactor = Math.max(MIN_EASE_FACTOR, this.easeFactor - 0.2f);
        return ReviewSchedule.builder()
                .id(this.id)
                .wordId(this.wordId)
                .intervalDays(1)
                .easeFactor(newEaseFactor)
                .nextReviewAt(clockHolder.today().plusDays(1))
                .updatedAt(clockHolder.now())
                .build();
    }
}
