package com.vocabin.api.word;

import com.vocabin.application.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Word", description = "단어 관리 API")
@RestController
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @Operation(summary = "단어 목록 조회")
    @GetMapping("/api/word-sets/{wordSetId}/words")
    public List<WordResponse> getWords(@PathVariable Long wordSetId) {
        return wordService.getWordList(wordSetId).stream()
                .map(WordResponse::from)
                .toList();
    }

    @Operation(summary = "단어 수정")
    @PutMapping("/api/words/{wordId}")
    public WordResponse updateWord(
            @PathVariable Long wordId,
            @RequestBody @Valid WordUpdateRequest request
    ) {
        return WordResponse.from(wordService.updateWord(wordId, request.english(), request.korean()));
    }

    @Operation(summary = "단어 삭제")
    @DeleteMapping("/api/words/{wordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWord(@PathVariable Long wordId) {
        wordService.deleteWord(wordId);
    }
}
