package com.vocabin.application.port.out;

import com.vocabin.domain.member.Member;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
    boolean existsByEmail(String email);
}
