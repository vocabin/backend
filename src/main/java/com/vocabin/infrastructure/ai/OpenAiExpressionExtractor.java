package com.vocabin.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.ExpressionExtractor;
import com.vocabin.domain.ai.EnglishSentence;
import com.vocabin.domain.ai.ExpressionSuggestion;
import com.vocabin.domain.ai.SuggestionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Calls the OpenAI Responses API with a strict JSON schema so the answer is always parseable
@Slf4j
@Component
public class OpenAiExpressionExtractor implements ExpressionExtractor {

    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final int MAX_SUGGESTIONS = 6;
    private static final int MAX_MEANINGS = 4;
    private static final int MAX_OUTPUT_TOKENS = 3000;

    static final String PROMPT_VERSION = "v2-multi-meaning";

    static final String INSTRUCTIONS = """
            너는 한국인 영어 학습자(중급)를 돕는 영어 해석 도우미다. 학습자가 영어 영상을 듣다가
            이해하지 못한 문장 하나를 준다. 그 문장에서 학습자가 모를 만한 숙어, 구동사, 유용한 표현,
            어려운 단어, 학습 가치가 있는 문법 구조를 뽑아 한국어 뜻과 예문을 준다.

            표현 고르는 규칙:
            - 학습자가 귀로 듣고 직접 입력한 문장이라 오타나 잘못 들은 부분이 있을 수 있다. 명백한 오류는
              자연스러운 문장으로 고쳐서 해석한다.
            - I, you, is, are 같은 기본 주어/be동사, 중학교 수준의 쉬운 단어는 뽑지 않는다.
            - 원어민이 실제로 자주 쓰는 구어체 표현(예: kind of, I'm down, gonna)도 뽑는다.
            - 중급 학습자가 모를 만한 단어(예: extraordinary, reluctant)는 type=VOCABULARY로 뽑는다.
              단, 이미 뽑은 숙어나 표현 안에 들어 있는 단어는 따로 뽑지 않는다.
            - 완료 시제(현재완료/과거완료/완료진행), 가정법, 조동사 + have p.p., 분사구문처럼 학습 가치가 있는
              문법 구조는 type=GRAMMAR로 뽑고 meaningKo 앞에 구조 이름을 쓴다 (예: "가정법: 마치 ~인 것처럼").
            - expression은 기본형으로 정리한다 (came across → come across). 같은 표현을 겹쳐 뽑지 않는다.
            - 중요한 순서로 최대 6개. 뽑을 만한 것이 없으면 빈 배열을 준다.

            뜻과 예문 규칙 (meanings):
            - meanings의 첫 번째 항목은 반드시 이 문장에서 쓰인 뜻이다. fromSentence=true로 하고,
              exampleEn에는 학습자가 입력한 문장을 그대로 쓴다 (명백한 오타만 고친다). exampleKo는 그 문장의 해석이다.
              학습자가 영상에서 본 문장을 예문으로 봐야 기억하기 쉽기 때문이다.
            - 그 표현이 다른 뜻으로도 흔히 쓰이면, 자주 쓰는 순서로 최대 3개의 뜻을 더 추가한다 (fromSentence=false).
              예: go through → 겪다 / 살펴보다 / 통과하다. 각 뜻마다 짧고 자연스러운 구어체 예문(15단어 이내)과 해석을 준다.
            - 뜻이 하나뿐인 표현은 meanings에 1개만 준다. 억지로 뜻을 늘리지 않는다.
            - meaningKo는 각각 20자 이내로 짧게 쓴다.
            - type: IDIOM(숙어), PHRASAL_VERB(구동사), EXPRESSION(유용한/구어체 표현), GRAMMAR(문법 구조), VOCABULARY(단어)
            """;

    private static final Map<String, Object> MEANING_SCHEMA = Map.of(
            "type", "object",
            "additionalProperties", false,
            "required", List.of("meaningKo", "exampleEn", "exampleKo", "fromSentence"),
            "properties", Map.of(
                    "meaningKo", Map.of("type", "string"),
                    "exampleEn", Map.of("type", "string"),
                    "exampleKo", Map.of("type", "string"),
                    "fromSentence", Map.of("type", "boolean")
            )
    );

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "additionalProperties", false,
            "required", List.of("items"),
            "properties", Map.of(
                    "items", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "additionalProperties", false,
                                    "required", List.of("expression", "type", "meanings"),
                                    "properties", Map.of(
                                            "expression", Map.of("type", "string"),
                                            "type", Map.of("type", "string",
                                                    "enum", List.of("IDIOM", "PHRASAL_VERB", "EXPRESSION", "GRAMMAR", "VOCABULARY")),
                                            "meanings", Map.of("type", "array", "items", MEANING_SCHEMA)
                                    )
                            )
                    )
            )
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiExpressionExtractor(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-6-luna}") String model) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<ExpressionSuggestion> extract(EnglishSentence sentence) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExpressionExtractionException("OPENAI_API_KEY is not configured", null);
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "instructions", INSTRUCTIONS,
                "input", sentence.value(),
                "max_output_tokens", MAX_OUTPUT_TOKENS,
                "text", Map.of("format", Map.of(
                        "type", "json_schema",
                        "name", "expressions",
                        "strict", true,
                        "schema", RESPONSE_SCHEMA))
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String response = restTemplate.postForObject(RESPONSES_URL, new HttpEntity<>(body, headers), String.class);
            return parse(response);
        } catch (ExpressionExtractionException e) {
            throw e;
        } catch (Exception e) {
            // Never log the request headers: they carry the API key
            log.error("OpenAI expression extraction failed: {}", e.getMessage());
            throw new ExpressionExtractionException("OpenAI request failed", e);
        }
    }

    @Override
    public String promptVersion() {
        return PROMPT_VERSION;
    }

    List<ExpressionSuggestion> parse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if ("incomplete".equals(root.path("status").asText())) {
                throw new ExpressionExtractionException("OpenAI response incomplete: " + root.path("incomplete_details"), null);
            }
            String text = null;
            for (JsonNode item : root.path("output")) {
                if (!"message".equals(item.path("type").asText())) continue;
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) text = content.path("text").asText();
                    if ("refusal".equals(content.path("type").asText())) {
                        log.warn("OpenAI refused to extract expressions: {}", content.path("refusal").asText());
                        return List.of();
                    }
                }
            }
            if (text == null) {
                throw new ExpressionExtractionException("OpenAI response has no output_text", null);
            }

            List<ExpressionSuggestion> suggestions = new ArrayList<>();
            for (JsonNode node : objectMapper.readTree(text).path("items")) {
                String expression = node.path("expression").asText("").trim();
                List<ExpressionSuggestion.Meaning> meanings = new ArrayList<>();
                for (JsonNode m : node.path("meanings")) {
                    String meaningKo = m.path("meaningKo").asText("").trim();
                    if (meaningKo.isEmpty()) continue;
                    meanings.add(new ExpressionSuggestion.Meaning(
                            meaningKo,
                            m.path("exampleEn").asText("").trim(),
                            m.path("exampleKo").asText("").trim(),
                            m.path("fromSentence").asBoolean(false)));
                    if (meanings.size() == MAX_MEANINGS) break;
                }
                if (expression.isEmpty() || meanings.isEmpty()) continue;
                suggestions.add(new ExpressionSuggestion(expression, toType(node.path("type").asText()), meanings));
                if (suggestions.size() == MAX_SUGGESTIONS) break;
            }
            return suggestions;
        } catch (ExpressionExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExpressionExtractionException("Cannot parse OpenAI response", e);
        }
    }

    private SuggestionType toType(String value) {
        try {
            return SuggestionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return SuggestionType.EXPRESSION;
        }
    }
}
