package com.vocabin.api.wordset;

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
}
