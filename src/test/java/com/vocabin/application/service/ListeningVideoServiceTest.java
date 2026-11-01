package com.vocabin.application.service;

import com.vocabin.domain.listening.ListeningVideo;
import com.vocabin.mock.AdjustableClockHolder;
import com.vocabin.mock.FakeListeningVideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListeningVideoServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final String VIDEO_A = "qvEOt1k4OMI";
    private static final String VIDEO_B = "dQw4w9WgXcQ";

    private AdjustableClockHolder clockHolder;
    private ListeningVideoServiceImpl service;

    @BeforeEach
    void setUp() {
        clockHolder = new AdjustableClockHolder(LocalDateTime.of(2026, 11, 2, 9, 0));
        service = new ListeningVideoServiceImpl(new FakeListeningVideoRepository(), clockHolder);
    }

    @Test
    void 처음_연_영상은_목록에_추가된다() {
        // given
        // when
        ListeningVideo video = service.open(MEMBER_ID, VIDEO_A, "베트남 브이로그");

        // then
        assertThat(service.getVideos(MEMBER_ID)).hasSize(1);
        assertThat(video.getTitle()).isEqualTo("베트남 브이로그");
        assertThat(video.getLastPositionSeconds()).isZero();
    }

    @Test
    void 다시_연_영상은_중복_없이_목록_맨_위로_올라간다() {
        // given
        service.open(MEMBER_ID, VIDEO_A, null);
        clockHolder.advance(Duration.ofMinutes(1));
        service.open(MEMBER_ID, VIDEO_B, null);
        clockHolder.advance(Duration.ofMinutes(1));

        // when
        service.open(MEMBER_ID, VIDEO_A, null);

        // then
        assertThat(service.getVideos(MEMBER_ID))
                .extracting(video -> video.getVideoId().value())
                .containsExactly(VIDEO_A, VIDEO_B);
    }

    @Test
    void 제목_없이_다시_열면_기존_제목을_유지한다() {
        // given
        service.open(MEMBER_ID, VIDEO_A, "베트남 브이로그");

        // when
        ListeningVideo reopened = service.open(MEMBER_ID, VIDEO_A, "  ");

        // then
        assertThat(reopened.getTitle()).isEqualTo("베트남 브이로그");
    }

    @Test
    void 이어보기_위치를_저장하고_다시_열어도_유지된다() {
        // given
        service.open(MEMBER_ID, VIDEO_A, null);

        // when
        service.savePosition(MEMBER_ID, VIDEO_A, 223);

        // then
        assertThat(service.open(MEMBER_ID, VIDEO_A, null).getLastPositionSeconds()).isEqualTo(223);
    }

    @Test
    void 올바르지_않은_영상_ID는_400으로_거부된다() {
        // given & when & then
        assertThatThrownBy(() -> service.open(MEMBER_ID, "../etc/passwd", null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 다른_회원의_영상은_위치_저장과_삭제를_할_수_없다() {
        // given
        service.open(MEMBER_ID, VIDEO_A, null);

        // when & then
        assertThatThrownBy(() -> service.savePosition(OTHER_MEMBER_ID, VIDEO_A, 10))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThatThrownBy(() -> service.remove(OTHER_MEMBER_ID, VIDEO_A))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(service.getVideos(MEMBER_ID)).hasSize(1);
    }

    @Test
    void 영상을_삭제하면_목록에서_사라진다() {
        // given
        service.open(MEMBER_ID, VIDEO_A, null);

        // when
        service.remove(MEMBER_ID, VIDEO_A);

        // then
        assertThat(service.getVideos(MEMBER_ID)).isEmpty();
    }

    @Test
    void 회원별로_목록이_분리된다() {
        // given
        service.open(MEMBER_ID, VIDEO_A, null);

        // when
        service.open(OTHER_MEMBER_ID, VIDEO_B, null);

        // then
        assertThat(service.getVideos(MEMBER_ID)).extracting(v -> v.getVideoId().value()).containsExactly(VIDEO_A);
        assertThat(service.getVideos(OTHER_MEMBER_ID)).extracting(v -> v.getVideoId().value()).containsExactly(VIDEO_B);
    }
}
