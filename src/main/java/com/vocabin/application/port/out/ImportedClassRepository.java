package com.vocabin.application.port.out;

import com.vocabin.domain.autoimport.ImportedClass;

import java.util.List;

public interface ImportedClassRepository {
    boolean existsByMemberIdAndExternalClassId(Long memberId, String externalClassId);
    ImportedClass save(ImportedClass importedClass);
    List<ImportedClass> findAllByMemberId(Long memberId);
}
