package com.vocabin.infrastructure.wordset;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WordSetJpaRepository extends JpaRepository<WordSetEntity, Long> {
}
