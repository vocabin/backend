package com.vocabin.application.port.out;

import com.vocabin.domain.schedule.ReviewSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewScheduleRepository {
    Optional<ReviewSchedule> findByWordId(Long wordId);
    ReviewSchedule save(ReviewSchedule reviewSchedule);
    List<Long> findDueWordIds(LocalDate today);
    Set<Long> findAllScheduledWordIds();
}
