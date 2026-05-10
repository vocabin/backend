package com.vocabin.domain.member;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Member {

    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
    private final LocalDateTime createdAt;

    public static Member create(String email, String encodedPassword, String nickname, ClockHolder clockHolder) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .createdAt(clockHolder.now())
                .build();
    }

    public Member updateNickname(String nickname) {
        return Member.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .nickname(nickname)
                .createdAt(this.createdAt)
                .build();
    }

    public Member updatePassword(String encodedPassword) {
        return Member.builder()
                .id(this.id)
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .createdAt(this.createdAt)
                .build();
    }
}
