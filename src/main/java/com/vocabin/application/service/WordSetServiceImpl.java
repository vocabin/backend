package com.vocabin.application.service;

import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.wordset.WordSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordSetServiceImpl implements WordSetService {

    private final WordSetRepository wordSetRepository;
    private final ClockHolder clockHolder;

    @Override
    public List<WordSet> getWordSets(Long memberId) {
        return wordSetRepository.findAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public WordSet createWordSet(String name, Long memberId) {
        WordSet wordSet = WordSet.create(name, memberId, clockHolder);
        return wordSetRepository.save(wordSet);
    }

    @Override
    @Transactional
    public void deleteWordSet(Long wordSetId, Long memberId) {
        WordSet wordSet = wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "단어 세트를 찾을 수 없습니다."));
        if (!wordSet.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }
        wordSetRepository.deleteById(wordSetId);
    }
}
