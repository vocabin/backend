package com.vocabin.api.pocket;

import com.vocabin.api.word.WordResponse;
import com.vocabin.application.service.PocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pocket", description = "포켓(중요 단어/숙어 모음) API")
@RestController
@RequestMapping("/api/pocket")
@RequiredArgsConstructor
public class PocketController {

    private final PocketService pocketService;

    @Operation(summary = "포켓 목록 조회", description = "포켓에 담은 표현을 최신순으로 반환합니다.")
    @GetMapping
    public List<WordResponse> getPocketWords(@RequestAttribute("memberId") Long memberId) {
        return WordResponse.fromList(pocketService.getPocketWords(memberId));
    }

    @Operation(summary = "포켓 오늘 복습 목록 조회", description = "복습일이 된 표현(ease_factor 낮은 순) → 신규 표현(랜덤) 순서로 반환합니다. 아직 복습일이 안 된 표현은 제외됩니다.")
    @GetMapping("/due")
    public List<WordResponse> getDueWords(@RequestAttribute("memberId") Long memberId) {
        return WordResponse.fromList(pocketService.getDueWords(memberId));
    }

    @Operation(summary = "포켓에 표현 추가", description = "영어 표현 기준으로 중복(대소문자·공백 무시)이면 409를 반환합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WordResponse add(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody @Valid PocketRequest request) {
        return WordResponse.from(pocketService.add(memberId, request.english(), request.korean(), request.memo()));
    }

    @Operation(summary = "포켓에서 표현 삭제")
    @DeleteMapping("/{wordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long wordId) {
        pocketService.remove(memberId, wordId);
    }
}
