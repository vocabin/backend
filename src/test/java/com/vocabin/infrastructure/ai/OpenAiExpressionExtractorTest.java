package com.vocabin.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.ExpressionExtractor.ExpressionExtractionException;
import com.vocabin.domain.ai.EnglishSentence;
import com.vocabin.domain.ai.ExpressionSuggestion;
import com.vocabin.domain.ai.SuggestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Parsing only — no network call is made in these tests
class OpenAiExpressionExtractorTest {

    private OpenAiExpressionExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new OpenAiExpressionExtractor(new RestTemplateBuilder(), new ObjectMapper(), "", "test-model");
    }

    private String response(String outputText) {
        String escaped = outputText.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return """
                {"status":"completed","output":[
                  {"type":"reasoning","summary":[]},
                  {"type":"message","content":[{"type":"output_text","text":"%s"}]}
                ]}""".formatted(escaped);
    }

    @Test
    void 응답에서_표현과_여러_뜻을_파싱한다() {
        // given
        String body = response("""
                {"items":[{"expression":"go through","type":"PHRASAL_VERB","meanings":[
                {"meaningKo":"겪다","exampleEn":"We went through a lot.","exampleKo":"우리는 많은 일을 겪었어.","fromSentence":true},
                {"meaningKo":"살펴보다","exampleEn":"Let's go through the list.","exampleKo":"목록을 살펴보자.","fromSentence":false}]}]}""");

        // when
        List<ExpressionSuggestion> result = extractor.parse(body);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).expression()).isEqualTo("go through");
        assertThat(result.get(0).type()).isEqualTo(SuggestionType.PHRASAL_VERB);
        assertThat(result.get(0).meanings()).extracting(ExpressionSuggestion.Meaning::meaningKo).containsExactly("겪다", "살펴보다");
        assertThat(result.get(0).meanings().get(0).fromSentence()).isTrue();
    }

    @Test
    void 표현은_최대_6개_뜻은_최대_4개까지만_받고_뜻이_없는_표현은_건너뛴다() {
        // given
        String meaning = "{\"meaningKo\":\"m\",\"exampleEn\":\"x\",\"exampleKo\":\"y\",\"fromSentence\":false}";
        String fiveMeanings = String.join(",", meaning, meaning, meaning, meaning, meaning);
        StringBuilder items = new StringBuilder("{\"expression\":\"no-meaning\",\"type\":\"IDIOM\",\"meanings\":[]}");
        for (int i = 0; i < 8; i++) {
            items.append(",{\"expression\":\"e").append(i).append("\",\"type\":\"IDIOM\",\"meanings\":[").append(fiveMeanings).append("]}");
        }
        String body = response("{\"items\":[" + items + "]}");

        // when
        List<ExpressionSuggestion> result = extractor.parse(body);

        // then
        assertThat(result).hasSize(6);
        assertThat(result.get(0).expression()).isEqualTo("e0");
        assertThat(result.get(0).meanings()).hasSize(4);
    }

    @Test
    void 단어_종류_VOCABULARY를_파싱한다() {
        // given
        String body = response("{\"items\":[{\"expression\":\"extraordinary\",\"type\":\"VOCABULARY\",\"meanings\":[{\"meaningKo\":\"특별한\",\"exampleEn\":\"It was an extraordinary event.\",\"exampleKo\":\"특별한 일이었다.\",\"fromSentence\":true}]}]}");

        // when
        List<ExpressionSuggestion> result = extractor.parse(body);

        // then
        assertThat(result).extracting(ExpressionSuggestion::type).containsExactly(SuggestionType.VOCABULARY);
    }

    @Test
    void 응답이_잘린_경우_실패로_처리한다() {
        // given
        String body = "{\"status\":\"incomplete\",\"incomplete_details\":{\"reason\":\"max_output_tokens\"},\"output\":[]}";

        // when & then
        assertThatThrownBy(() -> extractor.parse(body)).isInstanceOf(ExpressionExtractionException.class);
    }

    @Test
    void 모델이_거절하면_빈_목록을_준다() {
        // given
        String body = "{\"status\":\"completed\",\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"refusal\",\"refusal\":\"no\"}]}]}";

        // when & then
        assertThat(extractor.parse(body)).isEmpty();
    }

    @Test
    void API_키가_없으면_호출하지_않고_실패한다() {
        // given & when & then
        assertThatThrownBy(() -> extractor.extract(new EnglishSentence("Hello there.")))
                .isInstanceOf(ExpressionExtractionException.class);
    }
}
