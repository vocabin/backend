package com.vocabin.infrastructure.member;

import com.vocabin.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static MemberEntity from(Member member) {
        MemberEntity entity = new MemberEntity();
        entity.id = member.getId();
        entity.email = member.getEmail();
        entity.password = member.getPassword();
        entity.nickname = member.getNickname();
        entity.createdAt = member.getCreatedAt();
        return entity;
    }

    public Member toModel() {
        return Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .createdAt(createdAt)
                .build();
    }
}
