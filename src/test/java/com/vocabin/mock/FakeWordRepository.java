package com.vocabin.mock;

import com.vocabin.application.port.out.WordRepository;
import com.vocabin.domain.word.Word;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeWordRepository implements WordRepository {

    private final Map<Long, Word> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public List<Word> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Word> findAllByWordSetId(Long wordSetId) {
        return store.values().stream()
                .filter(word -> word.getWordSetId().equals(wordSetId))
                .toList();
    }

    @Override
    public List<Word> findAllByWordSetIds(List<Long> wordSetIds) {
        return store.values().stream()
                .filter(word -> wordSetIds.contains(word.getWordSetId()))
                .toList();
    }

    @Override
    public List<Word> findAllByIds(List<Long> ids) {
        return store.values().stream()
                .filter(word -> ids.contains(word.getId()))
                .toList();
    }

    @Override
    public Optional<Word> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Word save(Word word) {
        Long id = word.getId() != null ? word.getId() : idGenerator.incrementAndGet();
        Word saved = Word.builder()
                .id(id)
                .wordSetId(word.getWordSetId())
                .english(word.getEnglish())
                .korean(word.getKorean())
                .memo(word.getMemo())
                .createdAt(word.getCreatedAt())
                .build();
        store.put(id, saved);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
