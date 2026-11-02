package com.vocabin.application.service;

import com.vocabin.application.port.out.MemberRepository;
import com.vocabin.application.port.out.MemberSettingsRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.member.Member;
import com.vocabin.domain.member.MemberSettings;
import com.vocabin.infrastructure.auth.JwtProvider;
import com.vocabin.infrastructure.member.MemberEntity;
import com.vocabin.infrastructure.member.MemberJpaRepository;
import com.vocabin.infrastructure.member.RefreshTokenEntity;
import com.vocabin.infrastructure.member.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final MemberSettingsRepository memberSettingsRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ClockHolder clockHolder;

    @Value("${jwt.refresh-token-expiry-days:30}")
    private int refreshTokenExpiryDays;

    @Override
    @Transactional
    public TokenPair register(String email, String rawPassword, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        Member member = memberRepository.save(
                Member.create(email, passwordEncoder.encode(rawPassword), nickname, clockHolder)
        );
        memberSettingsRepository.save(MemberSettings.defaultFor(member.getId()));
        return issueTokenPair(member);
    }

    @Override
    @Transactional
    public TokenPair login(String email, String rawPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueTokenPair(member);
    }

    @Override
    @Transactional
    public TokenPair refresh(String refreshToken) {
        String tokenHash = sha256(refreshToken);

        // Invalid and expired tokens get the same 401 so the client simply sends the user to login
        RefreshTokenEntity entity = refreshTokenJpaRepository.findByTokenHash(tokenHash)
                .filter(found -> !found.getExpiresAt().isBefore(clockHolder.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 만료되었습니다. 다시 로그인해 주세요."));

        // Rotation: issue a new refresh token, retire the old one, and hand the new one back to the caller
        String newRawToken = UUID.randomUUID().toString();
        entity.rotate(sha256(newRawToken), clockHolder.now().plusDays(refreshTokenExpiryDays));

        return new TokenPair(jwtProvider.generateAccessToken(entity.getMember().getId()), newRawToken);
    }

    @Override
    @Transactional
    public void logout(Long memberId) {
        refreshTokenJpaRepository.deleteByMemberId(memberId);
    }

    private TokenPair issueTokenPair(Member member) {
        String accessToken = jwtProvider.generateAccessToken(member.getId());
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawRefreshToken);

        MemberEntity memberEntity = memberJpaRepository.findById(member.getId())
                .orElseThrow();

        refreshTokenJpaRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        entity -> entity.rotate(tokenHash, clockHolder.now().plusDays(refreshTokenExpiryDays)),
                        () -> refreshTokenJpaRepository.save(
                                RefreshTokenEntity.of(memberEntity, tokenHash, clockHolder.now().plusDays(refreshTokenExpiryDays), clockHolder.now())
                        )
                );

        return new TokenPair(accessToken, rawRefreshToken);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
