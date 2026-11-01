package com.vocabin.mock;

import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.domain.wordset.WordSet;
import com.vocabin.domain.wordset.WordSetType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeWordSetRepository implements WordSetRepository {

    private final Map<Long, WordSet> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public List<WordSet> findAllByMemberIdAndType(Long memberId, WordSetType type) {
        return store.values().stream()
                .filter(wordSet -> wordSet.getMemberId().equals(memberId) && wordSet.getType() == type)
                .toList();
    }

    @Override
    public Optional<WordSet> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public WordSet save(WordSet wordSet) {
        Long id = wordSet.getId() != null ? wordSet.getId() : idGenerator.incrementAndGet();
        WordSet saved = WordSet.builder()
                .id(id)
                .memberId(wordSet.getMemberId())
                .name(wordSet.getName())
                .type(wordSet.getType())
                .createdAt(wordSet.getCreatedAt())
                .build();
        store.put(id, saved);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
