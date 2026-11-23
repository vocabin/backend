package com.vocabin.domain.listening;

// One spoken sentence rebuilt from caption lines, with the time range it is spoken in
public record TranscriptSentence(double start, double end, String text) {
}
