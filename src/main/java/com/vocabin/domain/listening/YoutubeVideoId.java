package com.vocabin.domain.listening;

import java.util.regex.Pattern;

public record YoutubeVideoId(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[\\w-]{11}$");

    public YoutubeVideoId {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("올바른 유튜브 영상 ID가 아닙니다.");
        }
    }
}
