package com.vocabin.infrastructure.autoimport;

import com.vocabin.domain.autoimport.AutoImportConfig;
import com.vocabin.infrastructure.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auto_import_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoImportConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(nullable = false)
    private int dayOfWeek;

    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static AutoImportConfigEntity from(AutoImportConfig config, MemberEntity member) {
        AutoImportConfigEntity entity = new AutoImportConfigEntity();
        entity.id = config.getId();
        entity.member = member;
        entity.dayOfWeek = config.getDayOfWeek();
        entity.hour = config.getHour();
        entity.enabled = config.isEnabled();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    public AutoImportConfig toModel() {
        return AutoImportConfig.builder()
                .id(id)
                .memberId(member.getId())
                .dayOfWeek(dayOfWeek)
                .hour(hour)
                .enabled(enabled)
                .build();
    }
}
