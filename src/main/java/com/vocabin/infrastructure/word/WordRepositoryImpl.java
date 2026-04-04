package com.vocabin.infrastructure.word;

import com.vocabin.application.port.out.WordRepository;
import com.vocabin.domain.word.Word;
import com.vocabin.infrastructure.wordset.WordSetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WordRepositoryImpl implements WordRepository {

    private final WordJpaRepository wordJpaRepository;
    private final WordSetJpaRepository wordSetJpaRepository;

    @Override
    public List<Word> findAllByWordSetId(Long wordSetId) {
        return wordJpaRepository.findAllByWordSetId(wordSetId).stream()
                .map(WordEntity::toModel)
                .toList();
    }

    @Override
    public Optional<Word> findById(Long id) {
        return wordJpaRepository.findById(id)
                .map(WordEntity::toModel);
    }

    @Override
    public Word save(Word word) {
        var wordSetEntity = wordSetJpaRepository.findById(word.getWordSetId())
                .orElseThrow(() -> new NoSuchElementException("단어 세트를 찾을 수 없습니다. id=" + word.getWordSetId()));
        return wordJpaRepository.save(WordEntity.from(word, wordSetEntity)).toModel();
    }

    @Override
    public void deleteById(Long id) {
        wordJpaRepository.deleteById(id);
    }
}
