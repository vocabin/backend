package com.vocabin.api.auth;

import com.vocabin.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int REFRESH_TOKEN_EXPIRY_SECONDS = 30 * 24 * 60 * 60; // 30일

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "가입 후 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthService.TokenPair tokenPair = authService.register(request.email(), request.password(), request.nickname());
        setRefreshTokenCookie(response, tokenPair.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccessTokenResponse(tokenPair.accessToken()));
    }

    @Operation(summary = "로그인", description = "Access Token을 반환하고 Refresh Token을 HttpOnly 쿠키에 설정합니다.")
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.TokenPair tokenPair = authService.login(request.email(), request.password());
        setRefreshTokenCookie(response, tokenPair.refreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(tokenPair.accessToken()));
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token 쿠키로 새 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        String accessToken = authService.refresh(refreshToken);
        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestAttribute("memberId") Long memberId,
            HttpServletResponse response
    ) {
        authService.logout(memberId);
        deleteRefreshTokenCookie(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(REFRESH_TOKEN_EXPIRY_SECONDS);
        response.addCookie(cookie);
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new IllegalArgumentException("Refresh Token 쿠키가 없습니다.");
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token 쿠키가 없습니다."));
    }
}
