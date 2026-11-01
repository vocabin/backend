package com.vocabin.mock;

import com.vocabin.application.port.out.ReviewScheduleRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.domain.word.Word;
import com.vocabin.domain.wordset.WordSet;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeReviewScheduleRepository implements ReviewScheduleRepository {

    private final Map<Long, ReviewSchedule> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final WordRepository wordRepository;
    private final WordSetRepository wordSetRepository;

    public FakeReviewScheduleRepository(WordRepository wordRepository, WordSetRepository wordSetRepository) {
        this.wordRepository = wordRepository;
        this.wordSetRepository = wordSetRepository;
    }

    @Override
    public Optional<ReviewSchedule> findByWordId(Long wordId) {
        return store.values().stream()
                .filter(schedule -> schedule.getWordId().equals(wordId))
                .findFirst();
    }

    @Override
    public List<ReviewSchedule> findAllByWordIds(List<Long> wordIds) {
        return store.values().stream()
                .filter(schedule -> wordIds.contains(schedule.getWordId()))
                .toList();
    }

    @Override
    public ReviewSchedule save(ReviewSchedule reviewSchedule) {
        Long id = reviewSchedule.getId() != null ? reviewSchedule.getId() : idGenerator.incrementAndGet();
        ReviewSchedule saved = ReviewSchedule.builder()
                .id(id)
                .wordId(reviewSchedule.getWordId())
                .nextReviewAt(reviewSchedule.getNextReviewAt())
                .intervalDays(reviewSchedule.getIntervalDays())
                .easeFactor(reviewSchedule.getEaseFactor())
                .updatedAt(reviewSchedule.getUpdatedAt())
                .build();
        store.put(id, saved);
        return saved;
    }

    @Override
    public List<Long> findDueWordIds(LocalDate today, Long memberId) {
        return store.values().stream()
                .filter(schedule -> !schedule.getNextReviewAt().isAfter(today))
                .filter(schedule -> ownedBy(schedule.getWordId(), memberId))
                .sorted(Comparator.comparing(ReviewSchedule::getNextReviewAt))
                .map(ReviewSchedule::getWordId)
                .toList();
    }

    @Override
    public Set<Long> findAllScheduledWordIds(Long memberId) {
        return store.values().stream()
                .map(ReviewSchedule::getWordId)
                .filter(wordId -> ownedBy(wordId, memberId))
                .collect(Collectors.toSet());
    }

    private boolean ownedBy(Long wordId, Long memberId) {
        return wordRepository.findById(wordId)
                .map(Word::getWordSetId)
                .flatMap(wordSetRepository::findById)
                .map(WordSet::getMemberId)
                .filter(memberId::equals)
                .isPresent();
    }
}
