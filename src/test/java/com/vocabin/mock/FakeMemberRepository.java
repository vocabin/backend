package com.vocabin.mock;

import com.vocabin.application.port.out.MemberRepository;
import com.vocabin.domain.member.Member;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Member save(Member member) {
        Long id = member.getId() != null ? member.getId() : idGenerator.incrementAndGet();
        Member saved = Member.builder()
                .id(id)
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .createdAt(member.getCreatedAt())
                .build();
        store.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return store.values().stream().filter(m -> m.getEmail().equals(email)).findFirst();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
