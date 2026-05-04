package com.vocabin.infrastructure.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private MemberEntity member;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static RefreshTokenEntity of(MemberEntity member, String tokenHash, LocalDateTime expiresAt, LocalDateTime createdAt) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.member = member;
        entity.tokenHash = tokenHash;
        entity.expiresAt = expiresAt;
        entity.createdAt = createdAt;
        return entity;
    }

    public void rotate(String newTokenHash, LocalDateTime newExpiresAt) {
        this.tokenHash = newTokenHash;
        this.expiresAt = newExpiresAt;
    }
}
