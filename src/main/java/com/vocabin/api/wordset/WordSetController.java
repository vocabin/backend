package com.vocabin.api.wordset;

import com.vocabin.application.service.StatsService;
import com.vocabin.application.service.WordSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "WordSet", description = "단어 세트 관리 API")
@RestController
@RequestMapping("/api/word-sets")
@RequiredArgsConstructor
public class WordSetController {

    private final WordSetService wordSetService;
    private final StatsService statsService;

    @Operation(summary = "단어 세트 목록 조회")
    @GetMapping
    public List<WordSetResponse> getWordSets() {
        return wordSetService.getWordSets().stream()
                .map(WordSetResponse::from)
                .toList();
    }

    @Operation(summary = "단어 세트 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WordSetResponse createWordSet(@RequestBody @Valid WordSetRequest request) {
        return WordSetResponse.from(wordSetService.createWordSet(request.name()));
    }

    @Operation(summary = "세트별 진행률 조회", description = "각 단어 세트의 총 단어 수와 학습한 단어 수를 반환합니다.")
    @GetMapping("/progress")
    public List<WordSetProgressResponse> getProgress() {
        return statsService.getWordSetProgress().stream()
                .map(WordSetProgressResponse::from)
                .toList();
    }
}
