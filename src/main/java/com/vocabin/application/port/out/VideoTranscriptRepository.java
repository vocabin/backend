package com.vocabin.application.port.out;

import com.vocabin.domain.listening.VideoTranscript;
import com.vocabin.domain.listening.YoutubeVideoId;

import java.util.Optional;

public interface VideoTranscriptRepository {
    Optional<VideoTranscript> findByVideoId(YoutubeVideoId videoId);
    VideoTranscript save(VideoTranscript transcript);
}
