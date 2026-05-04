package com.vocabin.api.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access Token 응답")
public record AccessTokenResponse(
        @Schema(description = "Access Token (Bearer)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {}
