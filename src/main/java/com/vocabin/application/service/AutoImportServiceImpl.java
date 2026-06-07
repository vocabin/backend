package com.vocabin.application.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.AutoImportConfigRepository;
import com.vocabin.application.port.out.ImportedClassRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.autoimport.AutoImportConfig;
import com.vocabin.domain.autoimport.ImportedClass;
import com.vocabin.domain.word.Word;
import com.vocabin.domain.wordset.WordSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoImportServiceImpl implements AutoImportService {

    private static final String QUIZLET_API_URL =
            "https://fluent-five.vercel.app/api/quizlet/student/%EC%9D%B4%EB%AF%BC%EC%9A%B0";

    private final AutoImportConfigRepository configRepository;
    private final ImportedClassRepository importedClassRepository;
    private final WordSetRepository wordSetRepository;
    private final WordRepository wordRepository;
    private final ClockHolder clockHolder;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional(readOnly = true)
    public AutoImportConfig getConfig(Long memberId) {
        return configRepository.findByMemberId(memberId)
                .orElse(AutoImportConfig.defaultConfig(memberId));
    }

    @Override
    @Transactional
    public AutoImportConfig updateConfig(Long memberId, int dayOfWeek, int hour, boolean enabled) {
        AutoImportConfig existing = configRepository.findByMemberId(memberId)
                .orElse(AutoImportConfig.defaultConfig(memberId));
        return configRepository.save(existing.update(dayOfWeek, hour, enabled));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportedClass> getHistory(Long memberId) {
        return importedClassRepository.findAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public int fetchAndImport(Long memberId) {
        String json = restTemplate.getForObject(QUIZLET_API_URL, String.class);
        if (json == null) {
            log.warn("Auto-import: empty response from external API");
            return 0;
        }

        QuizletClass[] classes;
        try {
            classes = objectMapper.readValue(json, QuizletClass[].class);
        } catch (Exception e) {
            log.error("Auto-import: failed to parse response", e);
            return 0;
        }

        int imported = 0;
        for (QuizletClass qc : classes) {
            if (qc.id() == null || qc.engQuizlet() == null || qc.korQuizlet() == null) continue;
            if (importedClassRepository.existsByMemberIdAndExternalClassId(memberId, qc.id())) continue;

            String setName = qc.classDate() != null ? qc.classDate() : qc.id();
            WordSet wordSet = wordSetRepository.save(WordSet.create(setName, clockHolder));

            int wordCount = Math.min(qc.engQuizlet().size(), qc.korQuizlet().size());
            for (int i = 0; i < wordCount; i++) {
                String eng = qc.engQuizlet().get(i);
                String kor = qc.korQuizlet().get(i);
                if (eng == null || eng.isBlank() || kor == null || kor.isBlank()) continue;
                wordRepository.save(Word.create(wordSet.getId(), eng.trim(), kor.trim(), clockHolder));
            }

            importedClassRepository.save(
                    ImportedClass.create(memberId, qc.id(), wordSet.getId(), clockHolder.now()));
            imported++;
            log.info("Auto-import: imported class {} as WordSet {}", qc.id(), wordSet.getId());
        }

        return imported;
    }

    // ── Internal DTO for external API ──────────────────────────────────────────

    private record QuizletClass(
            @JsonProperty("_id") String id,
            @JsonProperty("class_date") String classDate,
            @JsonProperty("eng_quizlet") List<String> engQuizlet,
            @JsonProperty("kor_quizlet") List<String> korQuizlet
    ) {}
}
