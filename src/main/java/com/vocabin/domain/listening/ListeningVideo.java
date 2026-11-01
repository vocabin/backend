package com.vocabin.domain.listening;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// A YouTube video the member listens to in the listening note
@Getter
@Builder
public class ListeningVideo {

    private static final int MAX_TITLE_LENGTH = 300;

    private final Long id;
    private final Long memberId;
    private final YoutubeVideoId videoId;
    private final String title;
    private final int lastPositionSeconds;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ListeningVideo create(Long memberId, YoutubeVideoId videoId, String title, ClockHolder clockHolder) {
        return ListeningVideo.builder()
                .memberId(memberId)
                .videoId(videoId)
                .title(normalizeTitle(title))
                .lastPositionSeconds(0)
                .createdAt(clockHolder.now())
                .updatedAt(clockHolder.now())
                .build();
    }

    // Re-opening moves the video to the top of the list; a blank title keeps the known one
    public ListeningVideo reopen(String title, ClockHolder clockHolder) {
        String newTitle = normalizeTitle(title);
        return toBuilder()
                .title(newTitle != null ? newTitle : this.title)
                .updatedAt(clockHolder.now())
                .build();
    }

    public ListeningVideo recordPosition(int seconds, ClockHolder clockHolder) {
        return toBuilder()
                .lastPositionSeconds(Math.max(0, seconds))
                .updatedAt(clockHolder.now())
                .build();
    }

    private ListeningVideoBuilder toBuilder() {
        return ListeningVideo.builder()
                .id(this.id)
                .memberId(this.memberId)
                .videoId(this.videoId)
                .title(this.title)
                .lastPositionSeconds(this.lastPositionSeconds)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt);
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) return null;
        String trimmed = title.trim();
        return trimmed.length() > MAX_TITLE_LENGTH ? trimmed.substring(0, MAX_TITLE_LENGTH) : trimmed;
    }
}
