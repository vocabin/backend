package com.vocabin.infrastructure.wordset;

import com.vocabin.domain.wordset.WordSetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordSetJpaRepository extends JpaRepository<WordSetEntity, Long> {
    List<WordSetEntity> findAllByMemberIdAndTypeOrderByNameDesc(Long memberId, WordSetType type);
}
