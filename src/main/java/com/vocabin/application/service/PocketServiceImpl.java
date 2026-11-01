package com.vocabin.application.service;

import com.vocabin.application.port.out.ReviewScheduleRepository;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.schedule.ReviewSchedule;
import com.vocabin.domain.word.Word;
import com.vocabin.domain.wordset.WordSet;
import com.vocabin.domain.wordset.WordSetType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PocketServiceImpl implements PocketService {

    private final WordSetRepository wordSetRepository;
    private final WordRepository wordRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ClockHolder clockHolder;

    @Override
    @Transactional
    public Word add(Long memberId, String english, String korean, String memo) {
        return addIfAbsent(memberId, english, korean, memo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "이미 포켓에 담긴 표현입니다."));
    }

    @Override
    @Transactional
    public Optional<Word> addIfAbsent(Long memberId, String english, String korean, String memo) {
        WordSet pocket = ensurePocket(memberId);

        boolean duplicated = wordRepository.findAllByWordSetId(pocket.getId()).stream()
                .anyMatch(word -> word.isSameEnglish(english));
        if (duplicated) {
            return Optional.empty();
        }

        return Optional.of(wordRepository.save(
                Word.createPocketItem(pocket.getId(), english, korean, memo, clockHolder)));
    }

    @Override
    @Transactional
    public Long ensurePocketSetId(Long memberId) {
        return ensurePocket(memberId).getId();
    }

    @Override
    public List<Word> getPocketWords(Long memberId) {
        return findPocket(memberId)
                .map(pocket -> wordRepository.findAllByWordSetId(pocket.getId()).stream()
                        .sorted(Comparator.comparing(Word::getId).reversed())
                        .toList())
                .orElse(List.of());
    }

    @Override
    public List<Word> getDueWords(Long memberId) {
        List<Word> words = getPocketWords(memberId);
        Map<Long, ReviewSchedule> scheduleByWordId = reviewScheduleRepository
                .findAllByWordIds(words.stream().map(Word::getId).toList()).stream()
                .collect(Collectors.toMap(ReviewSchedule::getWordId, Function.identity()));
        LocalDate today = clockHolder.today();

        // 1순위: 복습일이 된 표현 — 못 외운 것(ease_factor 낮은 순)부터
        List<Word> dueWords = words.stream()
                .filter(word -> {
                    ReviewSchedule schedule = scheduleByWordId.get(word.getId());
                    return schedule != null && !schedule.getNextReviewAt().isAfter(today);
                })
                .sorted(Comparator
                        .comparing((Word word) -> scheduleByWordId.get(word.getId()).getEaseFactor())
                        .thenComparing(word -> scheduleByWordId.get(word.getId()).getNextReviewAt()))
                .toList();

        // 2순위: 아직 한 번도 복습하지 않은 신규 표현 — 랜덤
        List<Word> newWords = new ArrayList<>(words.stream()
                .filter(word -> !scheduleByWordId.containsKey(word.getId()))
                .toList());
        Collections.shuffle(newWords);

        List<Word> result = new ArrayList<>(dueWords);
        result.addAll(newWords);
        return result;
    }

    @Override
    @Transactional
    public void remove(Long memberId, Long wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "포켓 항목을 찾을 수 없습니다."));
        WordSet wordSet = wordSetRepository.findById(word.getWordSetId())
                .filter(WordSet::isPocket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "포켓 항목을 찾을 수 없습니다."));
        if (!wordSet.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }
        wordRepository.deleteById(wordId);
    }

    private WordSet ensurePocket(Long memberId) {
        return findPocket(memberId)
                .orElseGet(() -> wordSetRepository.save(WordSet.createPocket(memberId, clockHolder)));
    }

    private Optional<WordSet> findPocket(Long memberId) {
        return wordSetRepository.findAllByMemberIdAndType(memberId, WordSetType.POCKET).stream().findFirst();
    }
}
