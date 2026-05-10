package com.vocabin.api.upload;

import com.vocabin.domain.upload.UploadResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 업로드 결과")
public record UploadResponse(
        @Schema(description = "저장된 단어 수", example = "15")
        int savedCount
) {
    public static UploadResponse from(UploadResult result) {
        return new UploadResponse(result.savedCount());
    }
}
