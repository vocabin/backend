package com.vocabin.infrastructure.autoimport;

import com.vocabin.application.port.out.ImportedClassRepository;
import com.vocabin.domain.autoimport.ImportedClass;
import com.vocabin.infrastructure.member.MemberJpaRepository;
import com.vocabin.infrastructure.wordset.WordSetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ImportedClassRepositoryImpl implements ImportedClassRepository {

    private final ImportedClassJpaRepository jpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final WordSetJpaRepository wordSetJpaRepository;

    @Override
    public boolean existsByMemberIdAndExternalClassId(Long memberId, String externalClassId) {
        return jpaRepository.existsByMemberIdAndExternalClassId(memberId, externalClassId);
    }

    @Override
    public ImportedClass save(ImportedClass importedClass) {
        var member = memberJpaRepository.findById(importedClass.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        var wordSet = wordSetJpaRepository.findById(importedClass.getWordSetId())
                .orElseThrow(() -> new IllegalArgumentException("WordSet not found"));
        return jpaRepository.save(ImportedClassEntity.from(importedClass, member, wordSet)).toModel();
    }

    @Override
    public List<ImportedClass> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberIdOrderByImportedAtDesc(memberId)
                .stream()
                .map(ImportedClassEntity::toModel)
                .toList();
    }
}
