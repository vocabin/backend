package com.vocabin.infrastructure.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.TranscriptFetcher.FetchedTranscript;
import com.vocabin.application.port.out.TranscriptFetcher.TranscriptFetchException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Parsing only — the Python process is not started in these tests
class PythonTranscriptFetcherTest {

    private final PythonTranscriptFetcher fetcher = new PythonTranscriptFetcher(new ObjectMapper(), "python3");

    @Test
    void 스크립트_출력에서_자막_줄을_파싱한다() {
        // given
        String output = "{\"status\":\"ok\",\"language\":\"en\",\"generated\":true,\"lines\":[{\"text\":\"hello there\",\"start\":1.5,\"duration\":2.0}]}";

        // when
        Optional<FetchedTranscript> result = fetcher.parse(output);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().generated()).isTrue();
        assertThat(result.get().lines()).singleElement().satisfies(line -> {
            assertThat(line.text()).isEqualTo("hello there");
            assertThat(line.start()).isEqualTo(1.5);
        });
    }

    @Test
    void 영어_자막이_없다는_출력은_빈_결과다() {
        // given & when & then
        assertThat(fetcher.parse("{\"status\":\"none\"}")).isEmpty();
    }

    @Test
    void 해석할_수_없는_출력은_실패로_처리한다() {
        // given & when & then
        assertThatThrownBy(() -> fetcher.parse("Traceback (most recent call last)")).isInstanceOf(TranscriptFetchException.class);
    }
}
