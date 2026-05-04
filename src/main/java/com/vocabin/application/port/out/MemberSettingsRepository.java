package com.vocabin.application.port.out;

import com.vocabin.domain.member.MemberSettings;

import java.util.Optional;

public interface MemberSettingsRepository {
    MemberSettings save(MemberSettings settings);
    Optional<MemberSettings> findByMemberId(Long memberId);
}
