package com.vocabin.domain.listening;

// One caption line of a video: when it starts, how long it is shown, and what is said
public record TranscriptLine(double start, double duration, String text) {
}
