package com.vocabin.infrastructure.listening;

import com.vocabin.domain.listening.ListeningVideo;
import com.vocabin.domain.listening.YoutubeVideoId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "listening_video",
        uniqueConstraints = @UniqueConstraint(name = "uq_listening_video_member_video", columnNames = {"member_id", "video_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ListeningVideoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "video_id", nullable = false, length = 11)
    private String videoId;

    @Column(length = 300)
    private String title;

    @Column(name = "last_position_seconds", nullable = false)
    private int lastPositionSeconds;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static ListeningVideoEntity from(ListeningVideo video) {
        ListeningVideoEntity entity = new ListeningVideoEntity();
        entity.id = video.getId();
        entity.memberId = video.getMemberId();
        entity.videoId = video.getVideoId().value();
        entity.title = video.getTitle();
        entity.lastPositionSeconds = video.getLastPositionSeconds();
        entity.createdAt = video.getCreatedAt();
        entity.updatedAt = video.getUpdatedAt();
        return entity;
    }

    public ListeningVideo toModel() {
        return ListeningVideo.builder()
                .id(id)
                .memberId(memberId)
                .videoId(new YoutubeVideoId(videoId))
                .title(title)
                .lastPositionSeconds(lastPositionSeconds)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
