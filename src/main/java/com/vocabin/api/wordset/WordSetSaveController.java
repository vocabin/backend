package com.vocabin.api.wordset;

import com.vocabin.application.service.WordSetSaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "WordSetSave", description = "단어 세트 저장(즐겨찾기) API")
@RestController
@RequestMapping("/api/word-sets")
@RequiredArgsConstructor
public class WordSetSaveController {

    private final WordSetSaveService wordSetSaveService;

    @Operation(summary = "저장된 단어 세트 목록 조회")
    @GetMapping("/saved")
    public List<SavedWordSetResponse> getSavedWordSets(@RequestAttribute("memberId") Long memberId) {
        return wordSetSaveService.getSavedWordSets(memberId).stream()
                .map(SavedWordSetResponse::from)
                .toList();
    }

    @Operation(summary = "단어 세트 저장 여부 조회")
    @GetMapping("/{wordSetId}/saved")
    public Map<String, Boolean> isSaved(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long wordSetId) {
        return Map.of("saved", wordSetSaveService.isSaved(memberId, wordSetId));
    }

    @Operation(summary = "단어 세트 저장")
    @PostMapping("/{wordSetId}/save")
    @ResponseStatus(HttpStatus.CREATED)
    public SavedWordSetResponse saveWordSet(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long wordSetId) {
        return SavedWordSetResponse.from(wordSetSaveService.saveWordSet(memberId, wordSetId));
    }

    @Operation(summary = "단어 세트 저장 취소")
    @DeleteMapping("/{wordSetId}/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsaveWordSet(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long wordSetId) {
        wordSetSaveService.unsaveWordSet(memberId, wordSetId);
    }
}
