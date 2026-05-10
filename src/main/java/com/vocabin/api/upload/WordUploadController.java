package com.vocabin.api.upload;

import com.vocabin.application.service.WordUploadService;
import com.vocabin.domain.upload.UploadResult;
import com.vocabin.domain.upload.UploadType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Upload", description = "파일 업로드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/word-sets/{wordSetId}/upload")
public class WordUploadController {

    private final WordUploadService wordUploadService;

    @Operation(summary = "Quizlet 파일 업로드", description = "Quizlet에서 내보낸 PDF, CSV, Excel 파일을 업로드합니다.")
    @PostMapping(value = "/quizlet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadQuizlet(
            @PathVariable Long wordSetId,
            @RequestPart MultipartFile file
    ) throws IOException {
        UploadResult result = wordUploadService.upload(wordSetId, UploadType.QUIZLET, file);
        return ResponseEntity.ok(UploadResponse.from(result));
    }

    @Operation(summary = "템플릿 파일 업로드", description = "앱에서 제공한 템플릿 CSV 파일을 업로드합니다.")
    @PostMapping(value = "/template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadTemplate(
            @PathVariable Long wordSetId,
            @RequestPart MultipartFile file
    ) throws IOException {
        UploadResult result = wordUploadService.upload(wordSetId, UploadType.TEMPLATE, file);
        return ResponseEntity.ok(UploadResponse.from(result));
    }
}
