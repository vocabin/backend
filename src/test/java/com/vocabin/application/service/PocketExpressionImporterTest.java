package com.vocabin.application.service;

import com.vocabin.application.service.PocketExpressionImporter.DiarySummary;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.member.Member;
import com.vocabin.domain.word.Word;
import com.vocabin.mock.FakeImportedClassRepository;
import com.vocabin.mock.FakeMemberRepository;
import com.vocabin.mock.FakeReviewScheduleRepository;
import com.vocabin.mock.FakeWordRepository;
import com.vocabin.mock.FakeWordSetRepository;
import com.vocabin.mock.TestClockHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class PocketExpressionImporterTest {

    private static final String ALLOWED_EMAIL = "owner@example.com";
    private static final String EXPRESSIONS = "1. go on a date — 데이트하러 가다\n2. come across something — 우연히 발견하다";

    private FakeImportedClassRepository importedClassRepository;
    private FakeMemberRepository memberRepository;
    private PocketServiceImpl pocketService;
    private ClockHolder clockHolder;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        FakeWordSetRepository wordSetRepository = new FakeWordSetRepository();
        FakeWordRepository wordRepository = new FakeWordRepository();
        FakeReviewScheduleRepository reviewScheduleRepository =
                new FakeReviewScheduleRepository(wordRepository, wordSetRepository);
        clockHolder = new TestClockHolder(LocalDateTime.of(2026, 11, 5, 10, 0));
        importedClassRepository = new FakeImportedClassRepository();
        memberRepository = new FakeMemberRepository();
        pocketService = new PocketServiceImpl(wordSetRepository, wordRepository, reviewScheduleRepository, clockHolder);
        ownerId = memberRepository.save(Member.builder().email(ALLOWED_EMAIL).nickname("owner").build()).getId();
    }

    private PocketExpressionImporter importer(String allowedEmail) {
        return new PocketExpressionImporter(importedClassRepository, pocketService, memberRepository, clockHolder, allowedEmail);
    }

    @Test
    void 허용된_계정은_기준일_이후_일기의_표현이_수업_날짜_메모와_함께_포켓에_담긴다() {
        // given
        List<DiarySummary> diaries = List.of(new DiarySummary("d1", "2026-11-03"));

        // when
        int imported = importer(ALLOWED_EMAIL).importNewDiaries(ownerId, () -> diaries, id -> EXPRESSIONS);

        // then
        assertThat(imported).isEqualTo(1);
        assertThat(pocketService.getPocketWords(ownerId))
                .extracting(Word::getEnglish, Word::getMemo)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("go on a date", "2026-11-03 수업"),
                        org.assertj.core.groups.Tuple.tuple("come across something", "2026-11-03 수업"));
    }

    @Test
    void 기준일_이전_일기는_가져오지_않고_표현_조회도_하지_않는다() {
        // given
        List<DiarySummary> diaries = List.of(new DiarySummary("old", "2026-10-28"));
        AtomicInteger loaderCalls = new AtomicInteger();

        // when
        int imported = importer(ALLOWED_EMAIL).importNewDiaries(ownerId, () -> diaries, id -> {
            loaderCalls.incrementAndGet();
            return EXPRESSIONS;
        });

        // then
        assertThat(imported).isZero();
        assertThat(loaderCalls).hasValue(0);
        assertThat(pocketService.getPocketWords(ownerId)).isEmpty();
    }

    @Test
    void 허용되지_않은_계정은_아무것도_가져오지_않고_외부_조회도_하지_않는다() {
        // given
        Long strangerId = memberRepository.save(Member.builder().email("other@example.com").nickname("other").build()).getId();
        AtomicInteger diaryLoaderCalls = new AtomicInteger();

        // when
        int imported = importer(ALLOWED_EMAIL).importNewDiaries(strangerId, () -> {
            diaryLoaderCalls.incrementAndGet();
            return List.of(new DiarySummary("d1", "2026-11-03"));
        }, id -> EXPRESSIONS);

        // then
        assertThat(imported).isZero();
        assertThat(diaryLoaderCalls).hasValue(0);
        assertThat(pocketService.getPocketWords(strangerId)).isEmpty();
    }

    @Test
    void 허용_이메일_설정이_비어있으면_누구도_가져오지_않는다() {
        // given
        List<DiarySummary> diaries = List.of(new DiarySummary("d1", "2026-11-03"));

        // when
        int imported = importer("").importNewDiaries(ownerId, () -> diaries, id -> EXPRESSIONS);

        // then
        assertThat(imported).isZero();
        assertThat(pocketService.getPocketWords(ownerId)).isEmpty();
    }

    @Test
    void 이미_처리한_일기는_다시_가져오지_않아_삭제한_표현이_되살아나지_않는다() {
        // given
        List<DiarySummary> diaries = List.of(new DiarySummary("d1", "2026-11-03"));
        PocketExpressionImporter importer = importer(ALLOWED_EMAIL);
        importer.importNewDiaries(ownerId, () -> diaries, id -> EXPRESSIONS);
        pocketService.getPocketWords(ownerId).forEach(word -> pocketService.remove(ownerId, word.getId()));

        // when
        int imported = importer.importNewDiaries(ownerId, () -> diaries, id -> EXPRESSIONS);

        // then
        assertThat(imported).isZero();
        assertThat(pocketService.getPocketWords(ownerId)).isEmpty();
    }

    @Test
    void 표현_조회에_실패한_일기는_처리_완료로_기록하지_않아_다음에_다시_시도한다() {
        // given
        List<DiarySummary> diaries = List.of(new DiarySummary("d1", "2026-11-03"));
        PocketExpressionImporter importer = importer(ALLOWED_EMAIL);
        importer.importNewDiaries(ownerId, () -> diaries, id -> null);

        // when
        int imported = importer.importNewDiaries(ownerId, () -> diaries, id -> EXPRESSIONS);

        // then
        assertThat(imported).isEqualTo(1);
        assertThat(pocketService.getPocketWords(ownerId)).hasSize(2);
    }

    @Test
    void 이미_포켓에_있는_표현은_중복_저장하지_않는다() {
        // given
        pocketService.add(ownerId, "Go on a date", "데이트하다", null);
        List<DiarySummary> diaries = List.of(new DiarySummary("d1", "2026-11-03"));

        // when
        importer(ALLOWED_EMAIL).importNewDiaries(ownerId, () -> diaries, id -> EXPRESSIONS);

        // then
        List<String> english = new ArrayList<>(pocketService.getPocketWords(ownerId).stream().map(Word::getEnglish).toList());
        assertThat(english).containsExactlyInAnyOrder("Go on a date", "come across something");
    }
}
