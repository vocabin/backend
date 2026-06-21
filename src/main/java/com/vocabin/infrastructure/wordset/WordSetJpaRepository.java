package com.vocabin.infrastructure.wordset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordSetJpaRepository extends JpaRepository<WordSetEntity, Long> {
    List<WordSetEntity> findAllByMemberId(Long memberId);
}
