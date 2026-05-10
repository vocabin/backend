package com.vocabin.application.service;

import com.vocabin.application.port.out.MemberRepository;
import com.vocabin.application.port.out.MemberSettingsRepository;
import com.vocabin.domain.member.Member;
import com.vocabin.domain.member.MemberSettings;
import com.vocabin.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberSettingsRepository memberSettingsRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member getMe(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public Member updateNickname(Long memberId, String nickname) {
        Member member = getMe(memberId);
        return memberRepository.save(member.updateNickname(nickname));
    }

    @Override
    @Transactional
    public void updatePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = getMe(memberId);
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        memberRepository.save(member.updatePassword(passwordEncoder.encode(newPassword)));
    }

    @Override
    @Transactional
    public void deleteMe(Long memberId) {
        memberJpaRepository.deleteById(memberId);
    }

    @Override
    public MemberSettings getSettings(Long memberId) {
        return memberSettingsRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NoSuchElementException("설정을 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public MemberSettings updateSettings(Long memberId, int dailyGoal, boolean shuffle) {
        MemberSettings settings = getSettings(memberId);
        return memberSettingsRepository.save(settings.update(dailyGoal, shuffle));
    }
}
