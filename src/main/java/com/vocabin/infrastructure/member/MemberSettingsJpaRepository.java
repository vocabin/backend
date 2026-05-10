package com.vocabin.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSettingsJpaRepository extends JpaRepository<MemberSettingsEntity, Long> {
    Optional<MemberSettingsEntity> findByMemberId(Long memberId);
}
