package com.vocabin.application.service;

import com.vocabin.domain.autoimport.AutoImportConfig;
import com.vocabin.domain.autoimport.ImportedClass;

import java.util.List;

public interface AutoImportService {
    AutoImportConfig getConfig(Long memberId);
    AutoImportConfig updateConfig(Long memberId, int dayOfWeek, int hour, boolean enabled);
    List<ImportedClass> getHistory(Long memberId);
    int fetchAndImport(Long memberId);
}
