package com.vocabin.application.service;

import com.vocabin.domain.listening.TranscriptLine;
import com.vocabin.domain.listening.VideoTranscript;
import com.vocabin.mock.FakeTranscriptFetcher;
import com.vocabin.mock.FakeTranscriptFetcher.Mode;
import com.vocabin.mock.FakeVideoTranscriptRepository;
import com.vocabin.mock.TestClockHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TranscriptServiceTest {

    private static final String VIDEO_ID = "dQw4w9WgXcQ";

    private FakeTranscriptFetcher fetcher;
    private FakeVideoTranscriptRepository repository;
    private TranscriptServiceImpl service;

    @BeforeEach
    void setUp() {
        fetcher = new FakeTranscriptFetcher();
        repository = new FakeVideoTranscriptRepository();
        service = new TranscriptServiceImpl(fetcher, repository, new TestClockHolder(LocalDateTime.of(2026, 11, 23, 10, 0)));
    }

    @Test
    void 처음_요청하면_유튜브에서_영어_자막을_가져와_저장한다() {
        // given
        // when
        VideoTranscript transcript = service.getTranscript(VIDEO_ID);

        // then
        assertThat(transcript.isAvailable()).isTrue();
        assertThat(transcript.getLanguage()).isEqualTo("en");
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void 두_번째_요청부터는_저장된_자막을_쓰고_유튜브에_다시_요청하지_않는다() {
        // given
        service.getTranscript(VIDEO_ID);

        // when
        service.getTranscript(VIDEO_ID);

        // then
        assertThat(fetcher.calls()).isEqualTo(1);
    }

    @Test
    void 영어_자막이_없는_영상은_없음으로_저장되어_다시_요청하지_않는다() {
        // given
        fetcher.setMode(Mode.NO_ENGLISH);

        // when
        VideoTranscript first = service.getTranscript(VIDEO_ID);
        service.getTranscript(VIDEO_ID);

        // then
        assertThat(first.isAvailable()).isFalse();
        assertThat(fetcher.calls()).isEqualTo(1);
    }

    @Test
    void 가져오기에_실패하면_503이고_저장하지_않아_다음_요청에서_다시_시도한다() {
        // given
        fetcher.setMode(Mode.FAILING);

        // when & then
        assertThatThrownBy(() -> service.getTranscript(VIDEO_ID))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(repository.size()).isZero();

        fetcher.setMode(Mode.ENGLISH);
        assertThat(service.getTranscript(VIDEO_ID).isAvailable()).isTrue();
        assertThat(fetcher.calls()).isEqualTo(2);
    }

    @Test
    void 올바르지_않은_영상_ID는_400이고_유튜브에_요청하지_않는다() {
        // given & when & then
        assertThatThrownBy(() -> service.getTranscript("bad id; rm -rf"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(fetcher.calls()).isZero();
    }

    @Test
    void 효과음_줄은_빼고_두_줄로_나뉜_문장은_한_줄로_합친다() {
        // given
        // when
        VideoTranscript transcript = service.getTranscript(VIDEO_ID);

        // then
        assertThat(transcript.getLines()).extracting(TranscriptLine::text)
                .containsExactly("we went through a lot last year", "and it was kind of crazy");
    }
}
