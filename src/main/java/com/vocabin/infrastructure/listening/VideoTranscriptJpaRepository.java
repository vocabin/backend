package com.vocabin.infrastructure.listening;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoTranscriptJpaRepository extends JpaRepository<VideoTranscriptEntity, Long> {
    Optional<VideoTranscriptEntity> findByVideoId(String videoId);
}
