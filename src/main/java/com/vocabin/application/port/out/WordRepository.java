package com.vocabin.application.port.out;

import com.vocabin.domain.word.Word;

import java.util.List;
import java.util.Optional;

public interface WordRepository {
    List<Word> findAllByWordSetId(Long wordSetId);
    Optional<Word> findById(Long id);
    Word save(Word word);
    void deleteById(Long id);
}
