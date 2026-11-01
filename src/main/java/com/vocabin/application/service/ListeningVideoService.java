package com.vocabin.application.service;

import com.vocabin.domain.listening.ListeningVideo;

import java.util.List;

public interface ListeningVideoService {
    List<ListeningVideo> getVideos(Long memberId);
    ListeningVideo open(Long memberId, String videoId, String title);
    ListeningVideo savePosition(Long memberId, String videoId, int seconds);
    void remove(Long memberId, String videoId);
}
