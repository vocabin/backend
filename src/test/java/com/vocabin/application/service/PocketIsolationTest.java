package com.vocabin.application.service;

import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.domain.word.Word;
import com.vocabin.domain.wordset.WordSet;
import com.vocabin.mock.FakeReviewScheduleRepository;
import com.vocabin.mock.FakeStudyRecordRepository;
import com.vocabin.mock.FakeWordRepository;
import com.vocabin.mock.FakeWordSetRepository;
import com.vocabin.mock.TestClockHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies that pocket items never leak into the regular word-set features
class PocketIsolationTest {

    private static final Long MEMBER_ID = 1L;

    private FakeWordSetRepository wordSetRepository;
    private FakeWordRepository wordRepository;
    private FakeReviewScheduleRepository reviewScheduleRepository;
    private ClockHolder clockHolder;
    private PocketServiceImpl pocketService;
    private WordServiceImpl wordService;
    private WordSetServiceImpl wordSetService;

    @BeforeEach
    void setUp() {
        wordSetRepository = new FakeWordSetRepository();
        wordRepository = new FakeWordRepository();
        reviewScheduleRepository = new FakeReviewScheduleRepository(wordRepository, wordSetRepository);
        clockHolder = new TestClockHolder(LocalDateTime.of(2026, 11, 1, 10, 0));
        pocketService = new PocketServiceImpl(wordSetRepository, wordRepository, reviewScheduleRepository, clockHolder);
        wordService = new WordServiceImpl(wordRepository, wordSetRepository, new FakeStudyRecordRepository(),
                reviewScheduleRepository, clockHolder);
        wordSetService = new WordSetServiceImpl(wordSetRepository, clockHolder);
    }

    @Test
    void 일반_복습_목록에_포켓_단어가_섞이지_않는다() {
        // given
        WordSet weekSet = wordSetRepository.save(WordSet.create("1주차", MEMBER_ID, clockHolder));
        Word regular = wordRepository.save(Word.create(weekSet.getId(), "apple", "사과", clockHolder));
        Word pocket = pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);
        reviewScheduleRepository.save(ReviewSchedule.builder()
                .wordId(pocket.getId())
                .nextReviewAt(clockHolder.today())
                .intervalDays(1)
                .easeFactor(2.5f)
                .updatedAt(clockHolder.now())
                .build());

        // when
        List<Word> due = wordService.getDueWords(MEMBER_ID);
        List<Word> shuffled = wordService.getAllShuffled(MEMBER_ID);

        // then
        assertThat(due).extracting(Word::getId).containsExactly(regular.getId());
        assertThat(shuffled).extracting(Word::getId).containsExactly(regular.getId());
    }

    @Test
    void 단어_세트_목록에_포켓_세트가_나오지_않는다() {
        // given
        wordSetService.createWordSet("1주차", MEMBER_ID);
        pocketService.add(MEMBER_ID, "take care of", "~을 돌보다", null);

        // when
        List<WordSet> wordSets = wordSetService.getWordSets(MEMBER_ID);

        // then
        assertThat(wordSets).extracting(WordSet::getName).containsExactly("1주차");
    }
}
