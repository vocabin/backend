package com.vocabin.mock;

import com.vocabin.application.port.out.VideoTranscriptRepository;
import com.vocabin.domain.listening.VideoTranscript;
import com.vocabin.domain.listening.YoutubeVideoId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeVideoTranscriptRepository implements VideoTranscriptRepository {

    private final Map<String, VideoTranscript> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Optional<VideoTranscript> findByVideoId(YoutubeVideoId videoId) {
        return Optional.ofNullable(store.get(videoId.value()));
    }

    @Override
    public VideoTranscript save(VideoTranscript transcript) {
        VideoTranscript saved = VideoTranscript.builder()
                .id(transcript.getId() != null ? transcript.getId() : idGenerator.incrementAndGet())
                .videoId(transcript.getVideoId())
                .language(transcript.getLanguage())
                .generated(transcript.isGenerated())
                .lines(transcript.getLines())
                .fetchedAt(transcript.getFetchedAt())
                .build();
        store.put(saved.getVideoId().value(), saved);
        return saved;
    }

    public int size() {
        return store.size();
    }
}
