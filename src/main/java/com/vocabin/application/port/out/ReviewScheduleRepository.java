package com.vocabin.application.port.out;

import com.vocabin.domain.schedule.ReviewSchedule;

import java.util.Optional;

public interface ReviewScheduleRepository {
    Optional<ReviewSchedule> findByWordId(Long wordId);
    ReviewSchedule save(ReviewSchedule reviewSchedule);
}
