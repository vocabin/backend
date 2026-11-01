package com.vocabin.mock;

import com.vocabin.application.port.out.ImportedClassRepository;
import com.vocabin.domain.autoimport.ImportedClass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FakeImportedClassRepository implements ImportedClassRepository {

    private final List<ImportedClass> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public boolean existsByMemberIdAndExternalClassId(Long memberId, String externalClassId) {
        return store.stream().anyMatch(ic ->
                ic.getMemberId().equals(memberId) && ic.getExternalClassId().equals(externalClassId));
    }

    @Override
    public ImportedClass save(ImportedClass importedClass) {
        ImportedClass saved = ImportedClass.builder()
                .id(idGenerator.incrementAndGet())
                .memberId(importedClass.getMemberId())
                .externalClassId(importedClass.getExternalClassId())
                .wordSetId(importedClass.getWordSetId())
                .importedAt(importedClass.getImportedAt())
                .build();
        store.add(saved);
        return saved;
    }

    @Override
    public List<ImportedClass> findAllByMemberId(Long memberId) {
        return store.stream().filter(ic -> ic.getMemberId().equals(memberId)).toList();
    }
}
