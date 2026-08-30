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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoImportServiceImpl implements AutoImportService {

    private static final String LOGIN_URL = "https://api.davidsenglish.co.kr/auth/session/login";
    private static final String RECENT_CLASS_NOTE_URL =
            "https://api.davidsenglish.co.kr/learning-records/class-notes/recent?studentProfileId=%s";
    private static final String TODAY_QUIZ_URL =
            "https://api.davidsenglish.co.kr/learning-records/class-notes/%s/today-quiz";
    private static final String CSRF_TOKEN = "david-session-v1";

    @Value("${davidsenglish.phone}")
    private String phone;

    @Value("${davidsenglish.password}")
    private String password;

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
        LoginResponse login = login();
        if (login == null) {
            log.warn("Auto-import: login failed");
            return 0;
        }

        ClassNoteDto classNote = fetchRecentClassNote(login.accessToken(), login.user().studentProfileId());
        if (classNote == null || classNote.id() == null) {
            log.warn("Auto-import: no recent class note found");
            return 0;
        }

        if (importedClassRepository.existsByMemberIdAndExternalClassId(memberId, classNote.id())) {
            return 0;
        }

        List<QuizItem> items = fetchTodayQuiz(login.accessToken(), classNote.id());
        if (items == null || items.isEmpty()) {
            log.warn("Auto-import: empty quiz items for classNote {}", classNote.id());
            return 0;
        }

        String setName = classNote.classDate() != null ? classNote.classDate() : classNote.id();
        WordSet wordSet = wordSetRepository.save(WordSet.create(setName, memberId, clockHolder));

        for (QuizItem item : items) {
            if (item.english() == null || item.english().isBlank() || item.korean() == null || item.korean().isBlank()) continue;
            wordRepository.save(Word.create(wordSet.getId(), item.english().trim(), item.korean().trim(), clockHolder));
        }

        importedClassRepository.save(
                ImportedClass.create(memberId, classNote.id(), wordSet.getId(), clockHolder.now()));
        log.info("Auto-import: imported class {} as WordSet {}", classNote.id(), wordSet.getId());
        return 1;
    }

    private LoginResponse login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-csrf-token", CSRF_TOKEN);
        HttpEntity<LoginRequest> request = new HttpEntity<>(new LoginRequest(phone, password), headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(LOGIN_URL), HttpMethod.POST, request, String.class);
            return objectMapper.readValue(response.getBody(), LoginResponse.class);
        } catch (Exception e) {
            log.error("Auto-import: login failed", e);
            return null;
        }
    }

    private ClassNoteDto fetchRecentClassNote(String accessToken, String studentProfileId) {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(accessToken));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(String.format(RECENT_CLASS_NOTE_URL, studentProfileId)),
                    HttpMethod.GET, request, String.class);
            RecentClassNoteResponse body = objectMapper.readValue(response.getBody(), RecentClassNoteResponse.class);
            return body.classNote();
        } catch (Exception e) {
            log.error("Auto-import: failed to fetch recent class note", e);
            return null;
        }
    }

    private List<QuizItem> fetchTodayQuiz(String accessToken, String classNoteId) {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(accessToken));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(String.format(TODAY_QUIZ_URL, classNoteId)),
                    HttpMethod.GET, request, String.class);
            TodayQuizResponse body = objectMapper.readValue(response.getBody(), TodayQuizResponse.class);
            return body.items();
        } catch (Exception e) {
            log.error("Auto-import: failed to fetch today-quiz", e);
            return null;
        }
    }

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    // ── Internal DTOs for external API ──────────────────────────────────────────

    private record LoginRequest(String phone, String password) {}

    private record LoginResponse(
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("user") UserDto user
    ) {}

    private record UserDto(@JsonProperty("studentProfileId") String studentProfileId) {}

    private record RecentClassNoteResponse(@JsonProperty("classNote") ClassNoteDto classNote) {}

    private record ClassNoteDto(
            @JsonProperty("id") String id,
            @JsonProperty("classDate") String classDate
    ) {}

    private record TodayQuizResponse(@JsonProperty("items") List<QuizItem> items) {}

    private record QuizItem(
            @JsonProperty("korean") String korean,
            @JsonProperty("english") String english
    ) {}
}
