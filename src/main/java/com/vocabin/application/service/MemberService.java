package com.vocabin.application.service;

import com.vocabin.domain.member.Member;
import com.vocabin.domain.member.MemberSettings;

public interface MemberService {
    Member getMe(Long memberId);
    Member updateNickname(Long memberId, String nickname);
    void updatePassword(Long memberId, String currentPassword, String newPassword);
    void deleteMe(Long memberId);
    MemberSettings getSettings(Long memberId);
    MemberSettings updateSettings(Long memberId, int dailyGoal, boolean shuffle);
}
