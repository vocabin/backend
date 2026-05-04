package com.vocabin.domain.member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSettings {

    private final Long id;
    private final Long memberId;
    private final int dailyGoal;
    private final boolean shuffle;

    public static MemberSettings defaultFor(Long memberId) {
        return MemberSettings.builder()
                .memberId(memberId)
                .dailyGoal(20)
                .shuffle(true)
                .build();
    }

    public MemberSettings update(int dailyGoal, boolean shuffle) {
        return MemberSettings.builder()
                .id(this.id)
                .memberId(this.memberId)
                .dailyGoal(dailyGoal)
                .shuffle(shuffle)
                .build();
    }
}
