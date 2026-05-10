package com.vocabin.api.study;

import com.vocabin.application.service.StudyService;
import com.vocabin.domain.study.StudyRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Study", description = "학습 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    @Operation(summary = "학습 결과 기록", description = "O/X 결과를 저장하고 SM-2 복습 일정을 갱신합니다.")
    @PostMapping("/records")
    public ResponseEntity<StudyRecordResponse> record(@Valid @RequestBody StudyRecordRequest request) {
        StudyRecord studyRecord = studyService.record(request.wordId(), request.mode(), request.correct());
        return ResponseEntity.ok(StudyRecordResponse.from(studyRecord));
    }
}
