package com.vocabin.api.autoimport;

import com.vocabin.domain.autoimport.ImportedClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "임포트 이력 항목")
public record ImportHistoryResponse(
        @Schema(description = "외부 클래스 ID") String externalClassId,
        @Schema(description = "생성된 단어 세트 ID") Long wordSetId,
        @Schema(description = "임포트 시각") LocalDateTime importedAt
) {
    public static ImportHistoryResponse from(ImportedClass ic) {
        return new ImportHistoryResponse(ic.getExternalClassId(), ic.getWordSetId(), ic.getImportedAt());
    }
}
