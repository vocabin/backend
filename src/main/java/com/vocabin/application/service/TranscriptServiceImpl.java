package com.vocabin.application.service;

import com.vocabin.application.port.out.TranscriptFetcher;
import com.vocabin.application.port.out.VideoTranscriptRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.listening.VideoTranscript;
import com.vocabin.domain.listening.YoutubeVideoId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Captions are fetched from YouTube once per video and then served from the database
@Service
@RequiredArgsConstructor
public class TranscriptServiceImpl implements TranscriptService {

    private final TranscriptFetcher transcriptFetcher;
    private final VideoTranscriptRepository transcriptRepository;
    private final ClockHolder clockHolder;

    @Override
    @Transactional
    public VideoTranscript getTranscript(String videoIdText) {
        YoutubeVideoId videoId = toVideoId(videoIdText);
        return transcriptRepository.findByVideoId(videoId)
                .orElseGet(() -> transcriptRepository.save(fetch(videoId)));
    }

    private VideoTranscript fetch(YoutubeVideoId videoId) {
        try {
            return transcriptFetcher.fetchEnglish(videoId)
                    .map(f -> VideoTranscript.of(videoId, f.language(), f.generated(), f.lines(), clockHolder))
                    .orElseGet(() -> VideoTranscript.unavailable(videoId, clockHolder));
        } catch (TranscriptFetcher.TranscriptFetchException e) {
            // Not cached, so the next request tries again
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "자막을 불러오지 못했습니다.");
        }
    }

    private YoutubeVideoId toVideoId(String videoId) {
        try {
            return new YoutubeVideoId(videoId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
