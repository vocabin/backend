package com.vocabin.infrastructure.autoimport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutoImportConfigJpaRepository extends JpaRepository<AutoImportConfigEntity, Long> {
    Optional<AutoImportConfigEntity> findByMemberId(Long memberId);
    List<AutoImportConfigEntity> findAllByEnabledTrue();
}
