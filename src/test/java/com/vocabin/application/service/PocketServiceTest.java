package com.vocabin.application.service;

import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.domain.word.Word;
import com.vocabin.domain.wordset.WordSetType;
import com.vocabin.mock.FakeReviewScheduleRepository;
import com.vocabin.mock.FakeWordRepository;
import com.vocabin.mock.FakeWordSetRepository;
import com.vocabin.mock.TestClockHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PocketServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;

    private FakeWordSetRepository wordSetRepository;
    private FakeWordRepository wordRepository;
    private FakeReviewScheduleRepository reviewScheduleRepository;
    private ClockHolder clockHolder;
    private PocketServiceImpl pocketService;

    @BeforeEach
    void setUp() {
        wordSetRepository = new FakeWordSetRepository();
        wordRepository = new FakeWordRepository();
        reviewScheduleRepository = new FakeReviewScheduleRepository(wordRepository, wordSetRepository);
        clockHolder = new TestClockHolder(LocalDateTime.of(2026, 11, 1, 10, 0));
        pocketService = new PocketServiceImpl(wordSetRepository, wordRepository, reviewScheduleRepository, clockHolder);
    }

    @Test
    void 처음_담으면_포켓_세트가_자동_생성된다() {
        // given
        // no pocket set exists yet

        // when
        Word saved = pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);

        // then
        assertThat(wordSetRepository.findAllByMemberIdAndType(MEMBER_ID, WordSetType.POCKET)).hasSize(1);
        assertThat(saved.getWordSetId())
                .isEqualTo(wordSetRepository.findAllByMemberIdAndType(MEMBER_ID, WordSetType.POCKET).get(0).getId());
    }

    @Test
    void 두_번째부터는_기존_포켓_세트를_재사용한다() {
        // given
        pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);

        // when
        pocketService.add(MEMBER_ID, "come across", "우연히 발견하다", null);

        // then
        assertThat(wordSetRepository.findAllByMemberIdAndType(MEMBER_ID, WordSetType.POCKET)).hasSize(1);
        assertThat(pocketService.getPocketWords(MEMBER_ID)).hasSize(2);
    }

    @Test
    void 대소문자와_공백이_달라도_같은_영어_표현은_중복_저장할_수_없다() {
        // given
        pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);

        // when & then
        assertThatThrownBy(() -> pocketService.add(MEMBER_ID, "  Take   Care of ", "돌보다", null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void 빈_메모는_null로_저장된다() {
        // given
        // when
        Word saved = pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", "   ");

        // then
        assertThat(saved.getMemo()).isNull();
    }

    @Test
    void 오늘_복습_목록은_ease_factor가_낮은_표현부터_나온다() {
        // given
        Word easy = pocketService.add(MEMBER_ID, "easy one", "쉬운 것", null);
        Word hard = pocketService.add(MEMBER_ID, "hard one", "어려운 것", null);
        Word medium = pocketService.add(MEMBER_ID, "medium one", "보통", null);
        scheduleDue(easy, 2.5f);
        scheduleDue(hard, 1.3f);
        scheduleDue(medium, 1.9f);

        // when
        List<Word> due = pocketService.getDueWords(MEMBER_ID);

        // then
        assertThat(due).extracting(Word::getEnglish)
                .containsExactly("hard one", "medium one", "easy one");
    }

    @Test
    void 복습일이_안_된_표현은_제외되고_신규_표현은_복습_대상_뒤에_포함된다() {
        // given
        Word due = pocketService.add(MEMBER_ID, "due one", "복습 대상", null);
        Word notYetDue = pocketService.add(MEMBER_ID, "future one", "아직 아님", null);
        Word fresh = pocketService.add(MEMBER_ID, "fresh one", "신규", null);
        scheduleDue(due, 2.0f);
        reviewScheduleRepository.save(ReviewSchedule.builder()
                .wordId(notYetDue.getId())
                .nextReviewAt(clockHolder.today().plusDays(3))
                .intervalDays(3)
                .easeFactor(2.5f)
                .updatedAt(clockHolder.now())
                .build());

        // when
        List<Word> result = pocketService.getDueWords(MEMBER_ID);

        // then
        assertThat(result).extracting(Word::getEnglish).containsExactly("due one", "fresh one");
    }

    @Test
    void 다른_회원의_포켓_항목은_삭제할_수_없다() {
        // given
        Word word = pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);

        // when & then
        assertThatThrownBy(() -> pocketService.remove(OTHER_MEMBER_ID, word.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(pocketService.getPocketWords(MEMBER_ID)).hasSize(1);
    }

    @Test
    void 본인의_포켓_항목은_삭제할_수_있다() {
        // given
        Word word = pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);

        // when
        pocketService.remove(MEMBER_ID, word.getId());

        // then
        assertThat(pocketService.getPocketWords(MEMBER_ID)).isEmpty();
    }

    private void scheduleDue(Word word, float easeFactor) {
        reviewScheduleRepository.save(ReviewSchedule.builder()
                .wordId(word.getId())
                .nextReviewAt(clockHolder.today())
                .intervalDays(1)
                .easeFactor(easeFactor)
                .updatedAt(clockHolder.now())
                .build());
    }
}
