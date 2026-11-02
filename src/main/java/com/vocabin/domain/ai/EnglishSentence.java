package com.vocabin.domain.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

// A single sentence the learner heard; kept short so only that sentence is ever sent to the AI
public record EnglishSentence(String value) {

    public static final int MAX_LENGTH = 300;

    public EnglishSentence {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("문장을 입력해 주세요.");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("문장은 " + MAX_LENGTH + "자 이하로 입력해 주세요.");
        }
    }

    // Same sentence regardless of case or extra spaces -> same cache key
    public String cacheKey() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
