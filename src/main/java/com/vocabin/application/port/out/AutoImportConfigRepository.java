package com.vocabin.application.port.out;

import com.vocabin.domain.autoimport.AutoImportConfig;

import java.util.List;
import java.util.Optional;

public interface AutoImportConfigRepository {
    Optional<AutoImportConfig> findByMemberId(Long memberId);
    List<AutoImportConfig> findAllEnabled();
    AutoImportConfig save(AutoImportConfig config);
}
