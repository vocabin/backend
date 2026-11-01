package com.vocabin.api.listening;

import com.vocabin.domain.listening.ListeningVideo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "리스닝 영상 응답")
public record ListeningVideoResponse(
        @Schema(description = "유튜브 영상 ID", example = "qvEOt1k4OMI") String videoId,
        @Schema(description = "영상 제목 (아직 모르면 null)") String title,
        @Schema(description = "마지막으로 본 위치(초)") int lastPositionSeconds,
        @Schema(description = "처음 추가한 시간") LocalDateTime createdAt,
        @Schema(description = "마지막으로 연 시간") LocalDateTime updatedAt
) {
    public static ListeningVideoResponse from(ListeningVideo video) {
        return new ListeningVideoResponse(
                video.getVideoId().value(),
                video.getTitle(),
                video.getLastPositionSeconds(),
                video.getCreatedAt(),
                video.getUpdatedAt()
        );
    }
}
