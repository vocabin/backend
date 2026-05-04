package com.vocabin.infrastructure.member;

import com.vocabin.domain.member.MemberSettings;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private MemberEntity member;

    @Column(nullable = false)
    private int dailyGoal;

    @Column(nullable = false)
    private boolean shuffle;

    public static MemberSettingsEntity from(MemberSettings settings, MemberEntity member) {
        MemberSettingsEntity entity = new MemberSettingsEntity();
        entity.id = settings.getId();
        entity.member = member;
        entity.dailyGoal = settings.getDailyGoal();
        entity.shuffle = settings.isShuffle();
        return entity;
    }

    public MemberSettings toModel() {
        return MemberSettings.builder()
                .id(id)
                .memberId(member.getId())
                .dailyGoal(dailyGoal)
                .shuffle(shuffle)
                .build();
    }
}
