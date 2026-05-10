package com.vocabin.api.member;

import com.vocabin.application.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 계정 정보 조회")
    @GetMapping
    public ResponseEntity<MemberResponse> getMe(@RequestAttribute("memberId") Long memberId) {
        return ResponseEntity.ok(MemberResponse.from(memberService.getMe(memberId)));
    }

    @Operation(summary = "닉네임 수정")
    @PutMapping("/nickname")
    public ResponseEntity<MemberResponse> updateNickname(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        return ResponseEntity.ok(MemberResponse.from(memberService.updateNickname(memberId, request.nickname())));
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        memberService.updatePassword(memberId, request.currentPassword(), request.newPassword());
    }

    @Operation(summary = "회원 탈퇴", description = "계정 및 모든 관련 데이터를 삭제합니다.")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@RequestAttribute("memberId") Long memberId) {
        memberService.deleteMe(memberId);
    }
}
