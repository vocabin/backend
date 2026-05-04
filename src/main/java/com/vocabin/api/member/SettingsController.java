package com.vocabin.api.member;

import com.vocabin.application.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Settings", description = "학습 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings")
public class SettingsController {

    private final MemberService memberService;

    @Operation(summary = "학습 설정 조회")
    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(@RequestAttribute("memberId") Long memberId) {
        return ResponseEntity.ok(SettingsResponse.from(memberService.getSettings(memberId)));
    }

    @Operation(summary = "학습 설정 수정")
    @PutMapping
    public ResponseEntity<SettingsResponse> updateSettings(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody UpdateSettingsRequest request
    ) {
        return ResponseEntity.ok(SettingsResponse.from(
                memberService.updateSettings(memberId, request.dailyGoal(), request.shuffle())
        ));
    }
}
