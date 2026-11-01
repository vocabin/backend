package com.vocabin.infrastructure.listening;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListeningVideoJpaRepository extends JpaRepository<ListeningVideoEntity, Long> {
    List<ListeningVideoEntity> findAllByMemberIdOrderByUpdatedAtDesc(Long memberId);
    Optional<ListeningVideoEntity> findByMemberIdAndVideoId(Long memberId, String videoId);
}
