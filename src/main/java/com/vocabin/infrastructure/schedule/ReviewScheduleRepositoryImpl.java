package com.vocabin.infrastructure.schedule;

import com.vocabin.application.port.out.ReviewScheduleRepository;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.infrastructure.word.WordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReviewScheduleRepositoryImpl implements ReviewScheduleRepository {

    private final ReviewScheduleJpaRepository reviewScheduleJpaRepository;
    private final WordJpaRepository wordJpaRepository;

    @Override
    public Optional<ReviewSchedule> findByWordId(Long wordId) {
        return reviewScheduleJpaRepository.findByWordId(wordId)
                .map(ReviewScheduleEntity::toModel);
    }

    @Override
    public ReviewSchedule save(ReviewSchedule reviewSchedule) {
        var wordEntity = wordJpaRepository.findById(reviewSchedule.getWordId())
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + reviewSchedule.getWordId()));
        return reviewScheduleJpaRepository.save(ReviewScheduleEntity.from(reviewSchedule, wordEntity)).toModel();
    }

    @Override
    public List<Long> findDueWordIds(LocalDate today) {
        return reviewScheduleJpaRepository.findDueWordIds(today);
    }

    @Override
    public Set<Long> findAllScheduledWordIds() {
        return reviewScheduleJpaRepository.findAllScheduledWordIds();
    }
}
