package com.vocabin.mock;

import com.vocabin.application.port.out.TranscriptFetcher;
import com.vocabin.domain.listening.TranscriptLine;
import com.vocabin.domain.listening.YoutubeVideoId;

import java.util.List;
import java.util.Optional;

// Stands in for YouTube: counts calls so tests can prove captions are fetched only once
public class FakeTranscriptFetcher implements TranscriptFetcher {

    public enum Mode { ENGLISH, NO_ENGLISH, FAILING }

    private Mode mode = Mode.ENGLISH;
    private int calls = 0;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public int calls() {
        return calls;
    }

    @Override
    public Optional<FetchedTranscript> fetchEnglish(YoutubeVideoId videoId) {
        calls++;
        return switch (mode) {
            case ENGLISH -> Optional.of(new FetchedTranscript("en", true, List.of(
                    new TranscriptLine(1.0, 2.0, "[Music]"),
                    new TranscriptLine(3.0, 2.5, "we went through\na lot last year"),
                    new TranscriptLine(5.5, 2.0, "and it was kind of crazy"))));
            case NO_ENGLISH -> Optional.empty();
            case FAILING -> throw new TranscriptFetchException("blocked", null);
        };
    }
}
