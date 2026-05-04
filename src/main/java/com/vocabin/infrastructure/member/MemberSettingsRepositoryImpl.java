package com.vocabin.infrastructure.member;

import com.vocabin.application.port.out.MemberSettingsRepository;
import com.vocabin.domain.member.MemberSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberSettingsRepositoryImpl implements MemberSettingsRepository {

    private final MemberSettingsJpaRepository memberSettingsJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public MemberSettings save(MemberSettings settings) {
        MemberEntity memberEntity = memberJpaRepository.findById(settings.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다. id=" + settings.getMemberId()));
        return memberSettingsJpaRepository.save(MemberSettingsEntity.from(settings, memberEntity)).toModel();
    }

    @Override
    public Optional<MemberSettings> findByMemberId(Long memberId) {
        return memberSettingsJpaRepository.findByMemberId(memberId).map(MemberSettingsEntity::toModel);
    }
}
