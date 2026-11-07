package com.vocabin.application.service;

import com.vocabin.domain.member.Member;
import com.vocabin.mock.AdjustableClockHolder;
import com.vocabin.mock.FakeAiExplainLogRepository;
import com.vocabin.mock.FakeExpressionExtractor;
import com.vocabin.mock.FakeMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiExpressionServiceTest {

    private static final String ALLOWED_EMAIL = "owner@example.com";
    private static final String SENTENCE = "I came across an old friend yesterday.";

    private FakeExpressionExtractor extractor;
    private FakeAiExplainLogRepository logRepository;
    private FakeMemberRepository memberRepository;
    private AdjustableClockHolder clockHolder;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        extractor = new FakeExpressionExtractor();
        logRepository = new FakeAiExplainLogRepository();
        memberRepository = new FakeMemberRepository();
        clockHolder = new AdjustableClockHolder(LocalDateTime.of(2026, 11, 2, 21, 0));
        ownerId = memberRepository.save(Member.builder().email(ALLOWED_EMAIL).nickname("owner").build()).getId();
    }

    private AiExpressionServiceImpl service(String allowedEmail, int dailyLimit) {
        return new AiExpressionServiceImpl(extractor, logRepository, memberRepository, clockHolder, allowedEmail, dailyLimit);
    }

    private HttpStatusCode statusOf(Throwable e) {
        return ((ResponseStatusException) e).getStatusCode();
    }

    @Test
    void 허용된_계정은_문장에서_뽑은_표현을_받는다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);

        // when
        AiExpressionService.Result result = service.explain(ownerId, SENTENCE);

        // then
        assertThat(result.suggestions()).extracting(s -> s.expression()).containsExactly("come across");
        assertThat(result.cached()).isFalse();
        assertThat(result.remainingToday()).isEqualTo(29);
    }

    @Test
    void 허용되지_않은_계정은_403이고_AI를_호출하지_않는다() {
        // given
        Long strangerId = memberRepository.save(Member.builder().email("other@example.com").nickname("x").build()).getId();
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);

        // when & then
        assertThatThrownBy(() -> service.explain(strangerId, SENTENCE))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(extractor.calls()).isZero();
    }

    @Test
    void 허용_이메일_설정이_비어있으면_누구도_사용할_수_없다() {
        // given
        AiExpressionServiceImpl service = service("", 30);

        // when & then
        assertThatThrownBy(() -> service.explain(ownerId, SENTENCE))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(extractor.calls()).isZero();
    }

    @Test
    void 대소문자와_공백만_다른_같은_문장은_저장된_결과를_재사용해_AI를_다시_호출하지_않는다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);
        service.explain(ownerId, SENTENCE);

        // when
        AiExpressionService.Result again = service.explain(ownerId, "  i CAME across   an old friend yesterday. ");

        // then
        assertThat(again.cached()).isTrue();
        assertThat(extractor.calls()).isEqualTo(1);
        assertThat(again.remainingToday()).isEqualTo(29);
    }

    @Test
    void 빈_결과는_재사용하지_않고_같은_문장도_다시_AI에_물어본다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);
        extractor.returnEmptyResults();
        service.explain(ownerId, SENTENCE);

        // when
        AiExpressionService.Result again = service.explain(ownerId, SENTENCE);

        // then
        assertThat(again.cached()).isFalse();
        assertThat(extractor.calls()).isEqualTo(2);
        assertThat(again.remainingToday()).isEqualTo(28);
    }

    @Test
    void 첫_번째_뜻은_입력한_문장을_예문으로_가진다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);

        // when
        AiExpressionService.Result result = service.explain(ownerId, SENTENCE);

        // then
        var first = result.suggestions().get(0).meanings().get(0);
        assertThat(first.fromSentence()).isTrue();
        assertThat(first.exampleEn()).isEqualTo(SENTENCE);
    }

    @Test
    void 프롬프트_버전이_바뀌면_예전_결과를_재사용하지_않는다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);
        service.explain(ownerId, SENTENCE);
        extractor.changePromptVersion("test-v2");

        // when
        AiExpressionService.Result result = service.explain(ownerId, SENTENCE);

        // then
        assertThat(result.cached()).isFalse();
        assertThat(extractor.calls()).isEqualTo(2);
    }

    @Test
    void 하루_한도를_넘으면_429이고_AI를_호출하지_않는다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 2);
        service.explain(ownerId, "First sentence here.");
        service.explain(ownerId, "Second sentence here.");

        // when & then
        assertThatThrownBy(() -> service.explain(ownerId, "Third sentence here."))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        assertThat(extractor.calls()).isEqualTo(2);
    }

    @Test
    void 한도를_다_써도_이미_분석한_문장은_무료로_다시_볼_수_있다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 1);
        service.explain(ownerId, SENTENCE);

        // when
        AiExpressionService.Result cached = service.explain(ownerId, SENTENCE);

        // then
        assertThat(cached.cached()).isTrue();
        assertThat(cached.remainingToday()).isZero();
    }

    @Test
    void 다음날이_되면_한도가_초기화된다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 1);
        service.explain(ownerId, "First sentence here.");
        clockHolder.advance(Duration.ofHours(4)); // 21:00 -> next day 01:00

        // when
        AiExpressionService.Result result = service.explain(ownerId, "Second sentence here.");

        // then
        assertThat(result.cached()).isFalse();
        assertThat(extractor.calls()).isEqualTo(2);
    }

    @Test
    void 빈_문장이나_300자를_넘는_문장은_400이고_AI를_호출하지_않는다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);

        // when & then
        assertThatThrownBy(() -> service.explain(ownerId, "   "))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.explain(ownerId, "a".repeat(301)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(extractor.calls()).isZero();
    }

    @Test
    void AI_호출이_실패하면_503이고_사용_횟수에_포함되지_않는다() {
        // given
        AiExpressionServiceImpl service = service(ALLOWED_EMAIL, 30);
        extractor.failNextCalls();

        // when & then
        assertThatThrownBy(() -> service.explain(ownerId, SENTENCE))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(logRepository.countByMemberIdSince(ownerId, clockHolder.today().atStartOfDay())).isZero();
    }
}
