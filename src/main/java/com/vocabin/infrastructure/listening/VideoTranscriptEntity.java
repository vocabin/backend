package com.vocabin.infrastructure.listening;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_transcript",
        uniqueConstraints = @UniqueConstraint(name = "uq_video_transcript_video", columnNames = "video_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoTranscriptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false, length = 11)
    private String videoId;

    @Column(length = 16)
    private String language;

    @Column(nullable = false)
    private boolean generated;

    // A 30-60 minute video has hundreds of lines, so TEXT (64KB) is not enough
    @Column(name = "lines_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String linesJson;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    static VideoTranscriptEntity of(Long id, String videoId, String language, boolean generated,
                                    String linesJson, LocalDateTime fetchedAt) {
        VideoTranscriptEntity entity = new VideoTranscriptEntity();
        entity.id = id;
        entity.videoId = videoId;
        entity.language = language;
        entity.generated = generated;
        entity.linesJson = linesJson;
        entity.fetchedAt = fetchedAt;
        return entity;
    }
}
