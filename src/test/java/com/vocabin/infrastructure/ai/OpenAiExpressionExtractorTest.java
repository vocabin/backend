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
    void 응답의_output_text에서_표현_목록을_파싱한다() {
        // given
        String body = response("""
                {"items":[{"expression":"have been meaning to","meaningKo":"현재완료진행: ~하려고 계속 생각해 왔다",
                "exampleEn":"I've been meaning to call you.","exampleKo":"너한테 전화하려고 계속 생각했어.","type":"GRAMMAR"}]}""");

        // when
        List<ExpressionSuggestion> result = extractor.parse(body);

        // then
        assertThat(result).containsExactly(new ExpressionSuggestion(
                "have been meaning to", "현재완료진행: ~하려고 계속 생각해 왔다",
                "I've been meaning to call you.", "너한테 전화하려고 계속 생각했어.", SuggestionType.GRAMMAR));
    }

    @Test
    void 최대_6개까지만_받고_빈_표현은_건너뛴다() {
        // given
        StringBuilder items = new StringBuilder("{\"expression\":\" \",\"meaningKo\":\"\",\"exampleEn\":\"\",\"exampleKo\":\"\",\"type\":\"IDIOM\"}");
        for (int i = 0; i < 8; i++) {
            items.append(",{\"expression\":\"e").append(i).append("\",\"meaningKo\":\"m\",\"exampleEn\":\"x\",\"exampleKo\":\"y\",\"type\":\"IDIOM\"}");
        }
        String body = response("{\"items\":[" + items + "]}");

        // when
        List<ExpressionSuggestion> result = extractor.parse(body);

        // then
        assertThat(result).hasSize(6);
        assertThat(result.get(0).expression()).isEqualTo("e0");
    }

    @Test
    void 단어_종류_VOCABULARY를_파싱한다() {
        // given
        String body = response("{\"items\":[{\"expression\":\"extraordinary\",\"meaningKo\":\"특별한, 비범한\",\"exampleEn\":\"It was an extraordinary day.\",\"exampleKo\":\"특별한 하루였다.\",\"type\":\"VOCABULARY\"}]}");

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
