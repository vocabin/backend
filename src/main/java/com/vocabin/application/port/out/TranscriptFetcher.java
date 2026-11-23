package com.vocabin.application.port.out;

import com.vocabin.domain.listening.TranscriptLine;
import com.vocabin.domain.listening.YoutubeVideoId;

import java.util.List;
import java.util.Optional;

public interface TranscriptFetcher {

    // Empty when the video has no English captions; throws TranscriptFetchException when fetching itself failed
    Optional<FetchedTranscript> fetchEnglish(YoutubeVideoId videoId);

    record FetchedTranscript(String language, boolean generated, List<TranscriptLine> lines) {}

    class TranscriptFetchException extends RuntimeException {
        public TranscriptFetchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
