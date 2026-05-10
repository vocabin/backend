package com.vocabin.application.service;

public interface AuthService {
    TokenPair register(String email, String rawPassword, String nickname);
    TokenPair login(String email, String rawPassword);
    String refresh(String refreshToken);
    void logout(Long memberId);

    record TokenPair(String accessToken, String refreshToken) {}
}
