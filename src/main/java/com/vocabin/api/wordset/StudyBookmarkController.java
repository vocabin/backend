package com.vocabin.api.wordset;

import com.vocabin.application.service.StudyBookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "StudyBookmark", description = "학습 책갈피 API")
@RestController
@RequestMapping("/api/word-sets")
@RequiredArgsConstructor
public class StudyBookmarkController {

    private final StudyBookmarkService studyBookmarkService;

    @Operation(summary = "책갈피 조회", description = "해당 단어 세트의 마지막 학습 위치를 반환합니다. 없으면 204.")
    @GetMapping("/{wordSetId}/bookmark")
    public ResponseEntity<StudyBookmarkResponse> getBookmark(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long wordSetId) {
        return studyBookmarkService.getBookmark(memberId, wordSetId)
                .map(b -> ResponseEntity.ok(StudyBookmarkResponse.from(b)))
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "책갈피 저장/업데이트", description = "현재 학습 위치를 저장합니다.")
    @PutMapping("/{wordSetId}/bookmark")
    public StudyBookmarkResponse upsertBookmark(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long wordSetId,
            @RequestBody @Valid UpdateBookmarkRequest request) {
        return StudyBookmarkResponse.from(
                studyBookmarkService.upsertBookmark(memberId, wordSetId, request.wordIndex()));
    }
}
