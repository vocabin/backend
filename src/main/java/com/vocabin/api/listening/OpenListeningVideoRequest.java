package com.vocabin.api.listening;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "리스닝 영상 열기(목록 추가/갱신) 요청")
public record OpenListeningVideoRequest(
        @Schema(description = "영상 제목 (모르면 생략, 빈 값이면 기존 제목 유지)", example = "베트남 가성비 도시 브이로그")
        @Size(max = 300) String title
) {}
