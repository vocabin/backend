package com.vocabin.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByMemberId(Long memberId);
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    void deleteByMemberId(Long memberId);
}
