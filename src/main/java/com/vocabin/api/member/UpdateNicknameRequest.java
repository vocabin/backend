package com.vocabin.api.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 수정 요청")
public record UpdateNicknameRequest(
        @Schema(description = "새 닉네임", example = "민우")
        @NotBlank @Size(max = 50) String nickname
) {}
