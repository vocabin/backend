package com.vocabin.application.service;

import com.vocabin.application.port.out.ReviewScheduleRepository;
import com.vocabin.application.port.out.StudyRecordRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.word.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final WordSetRepository wordSetRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ClockHolder clockHolder;

    @Override
    public List<Word> getDueWords() {
        // 1순위: 복습 예정 단어 (nextReviewAt <= 오늘) — 날짜 오름차순
        List<Long> dueWordIds = reviewScheduleRepository.findDueWordIds(clockHolder.today());
        Set<Long> scheduledWordIds = reviewScheduleRepository.findAllScheduledWordIds();

        List<Word> allWords = wordRepository.findAll();
        Map<Long, Word> wordById = allWords.stream().collect(Collectors.toMap(Word::getId, w -> w));

        List<Word> dueWords = dueWordIds.stream()
                .map(wordById::get)
                .filter(Objects::nonNull)
                .toList();

        // 2순위: 신규 단어 (ReviewSchedule 없음) — 랜덤
        List<Word> newWords = new ArrayList<>(allWords.stream()
                .filter(w -> !scheduledWordIds.contains(w.getId()))
                .toList());
        Collections.shuffle(newWords);

        // 3순위: 아직 복습 안 해도 되는 단어 — 랜덤
        Set<Long> dueWordIdSet = new HashSet<>(dueWordIds);
        List<Word> notYetDueWords = new ArrayList<>(allWords.stream()
                .filter(w -> scheduledWordIds.contains(w.getId()) && !dueWordIdSet.contains(w.getId()))
                .toList());
        Collections.shuffle(notYetDueWords);

        List<Word> result = new ArrayList<>();
        result.addAll(dueWords);
        result.addAll(newWords);
        result.addAll(notYetDueWords);
        return result;
    }

    @Override
    public List<Word> getAllShuffled() {
        List<Word> words = new ArrayList<>(wordRepository.findAll());
        Collections.shuffle(words);
        return words;
    }

    @Override
    public List<Word> getWordList(Long wordSetId) {
        wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new NoSuchElementException("단어 세트를 찾을 수 없습니다. id=" + wordSetId));
        return wordRepository.findAllByWordSetId(wordSetId);
    }

    @Override
    public List<Word> getWeakWords(Long wordSetId) {
        List<Long> wordIds = studyRecordRepository.findWeakWordIds(wordSetId);
        if (wordIds.isEmpty()) {
            return List.of();
        }
        // wordIds는 오답 수 내림차순으로 정렬된 상태 — 조회 후 순서 복원
        List<Word> words = wordRepository.findAllByIds(wordIds);
        return wordIds.stream()
                .map(id -> words.stream().filter(w -> w.getId().equals(id)).findFirst().orElseThrow())
                .toList();
    }

    @Override
    @Transactional
    public Word updateWord(Long wordId, String english, String korean) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + wordId));
        return wordRepository.save(word.update(english, korean));
    }

    @Override
    @Transactional
    public void deleteWord(Long wordId) {
        wordRepository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("단어를 찾을 수 없습니다. id=" + wordId));
        wordRepository.deleteById(wordId);
    }
}
