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
        return WordResponse.fromList(wordService.getWordList(wordSetId));
    }

    @Operation(summary = "취약 단어 목록 조회", description = "오답 3회 이상인 단어를 오답 수 내림차순으로 반환합니다. wordSetId로 세트 필터 가능.")
    @GetMapping("/api/words/weak")
    public List<WordResponse> getWeakWords(@RequestParam(required = false) Long wordSetId) {
        return WordResponse.fromList(wordService.getWeakWords(wordSetId));
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
