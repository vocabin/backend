package com.vocabin.api.pocket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "포켓 표현 추가 요청")
public record PocketRequest(
        @Schema(description = "영어 단어/숙어", example = "take care of")
        @NotBlank @Size(max = 500) String english,

        @Schema(description = "한국어 뜻", example = "~을 돌보다")
        @NotBlank @Size(max = 500) String korean,

        @Schema(description = "메모 (선택)", example = "중요! 시험에 자주 나옴")
        @Size(max = 500) String memo
) {}
