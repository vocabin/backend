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
    private static final String QUIZLET_SETS_URL =
            "https://api.davidsenglish.co.kr/learning-records/quizlets?limit=100&studentProfileId=%s";
    private static final String QUIZLET_SET_DETAIL_URL =
            "https://api.davidsenglish.co.kr/learning-records/quizlets/%s?studentProfileId=%s";
    private static final String CSRF_TOKEN = "david-session-v1";
    private static final String APP_ORIGIN = "https://app.davidsenglish.co.kr";

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
        String accessToken = login.accessToken();
        String studentProfileId = login.user().studentProfileId();

        List<QuizletSetSummary> quizletSets = fetchQuizletSets(accessToken, studentProfileId);
        if (quizletSets == null || quizletSets.isEmpty()) {
            log.warn("Auto-import: no quizlet sets found");
            return 0;
        }

        int imported = 0;
        for (QuizletSetSummary summary : quizletSets) {
            if (importedClassRepository.existsByMemberIdAndExternalClassId(memberId, summary.id())) continue;

            QuizletSetDetail detail = fetchQuizletSetDetail(accessToken, studentProfileId, summary.id());
            if (detail == null || detail.cards() == null) continue;

            List<QuizletCard> favoriteCards = detail.cards().stream().filter(QuizletCard::favorite).toList();
            if (favoriteCards.isEmpty()) continue;

            String setName = summary.classDate() != null ? summary.classDate() : summary.id();
            WordSet wordSet = wordSetRepository.save(WordSet.create(setName, memberId, clockHolder));

            for (QuizletCard card : favoriteCards) {
                if (card.englishText() == null || card.englishText().isBlank()
                        || card.koreanText() == null || card.koreanText().isBlank()) continue;
                wordRepository.save(Word.create(wordSet.getId(), card.englishText().trim(), card.koreanText().trim(), clockHolder));
            }

            importedClassRepository.save(
                    ImportedClass.create(memberId, summary.id(), wordSet.getId(), clockHolder.now()));
            imported++;
            log.info("Auto-import: imported quizlet set {} ({} favorite cards) as WordSet {}",
                    summary.id(), favoriteCards.size(), wordSet.getId());
        }

        return imported;
    }

    private LoginResponse login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-csrf-token", CSRF_TOKEN);
        headers.set(HttpHeaders.ORIGIN, APP_ORIGIN);
        headers.set(HttpHeaders.REFERER, APP_ORIGIN + "/");
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

    private List<QuizletSetSummary> fetchQuizletSets(String accessToken, String studentProfileId) {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(accessToken));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(String.format(QUIZLET_SETS_URL, studentProfileId)),
                    HttpMethod.GET, request, String.class);
            QuizletSetsResponse body = objectMapper.readValue(response.getBody(), QuizletSetsResponse.class);
            return body.quizletSets();
        } catch (Exception e) {
            log.error("Auto-import: failed to fetch quizlet sets", e);
            return null;
        }
    }

    private QuizletSetDetail fetchQuizletSetDetail(String accessToken, String studentProfileId, String quizletSetId) {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(accessToken));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(String.format(QUIZLET_SET_DETAIL_URL, quizletSetId, studentProfileId)),
                    HttpMethod.GET, request, String.class);
            QuizletSetDetailResponse body = objectMapper.readValue(response.getBody(), QuizletSetDetailResponse.class);
            return body.quizletSet();
        } catch (Exception e) {
            log.error("Auto-import: failed to fetch quizlet set detail for {}", quizletSetId, e);
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

    private record QuizletSetsResponse(@JsonProperty("quizletSets") List<QuizletSetSummary> quizletSets) {}

    private record QuizletSetSummary(
            @JsonProperty("id") String id,
            @JsonProperty("classDate") String classDate
    ) {}

    private record QuizletSetDetailResponse(@JsonProperty("quizletSet") QuizletSetDetail quizletSet) {}

    private record QuizletSetDetail(@JsonProperty("cards") List<QuizletCard> cards) {}

    private record QuizletCard(
            @JsonProperty("englishText") String englishText,
            @JsonProperty("koreanText") String koreanText,
            @JsonProperty("favorite") boolean favorite
    ) {}
}
