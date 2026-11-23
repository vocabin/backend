package com.vocabin.application.service;

import com.vocabin.domain.listening.VideoTranscript;

public interface TranscriptService {
    VideoTranscript getTranscript(String videoId);
}
