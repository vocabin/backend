package com.vocabin.infrastructure.autoimport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportedClassJpaRepository extends JpaRepository<ImportedClassEntity, Long> {
    boolean existsByMemberIdAndExternalClassId(Long memberId, String externalClassId);
    List<ImportedClassEntity> findAllByMemberIdOrderByImportedAtDesc(Long memberId);
}
