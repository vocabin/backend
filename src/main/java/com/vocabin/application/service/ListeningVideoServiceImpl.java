package com.vocabin.application.service;

import com.vocabin.application.port.out.ListeningVideoRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.listening.ListeningVideo;
import com.vocabin.domain.listening.YoutubeVideoId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListeningVideoServiceImpl implements ListeningVideoService {

    private final ListeningVideoRepository listeningVideoRepository;
    private final ClockHolder clockHolder;

    @Override
    public List<ListeningVideo> getVideos(Long memberId) {
        return listeningVideoRepository.findAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public ListeningVideo open(Long memberId, String videoId, String title) {
        YoutubeVideoId id = toVideoId(videoId);
        ListeningVideo video = listeningVideoRepository.findByMemberIdAndVideoId(memberId, id)
                .map(existing -> existing.reopen(title, clockHolder))
                .orElseGet(() -> ListeningVideo.create(memberId, id, title, clockHolder));
        return listeningVideoRepository.save(video);
    }

    @Override
    @Transactional
    public ListeningVideo savePosition(Long memberId, String videoId, int seconds) {
        return listeningVideoRepository.save(find(memberId, videoId).recordPosition(seconds, clockHolder));
    }

    @Override
    @Transactional
    public void remove(Long memberId, String videoId) {
        listeningVideoRepository.delete(find(memberId, videoId));
    }

    // Lookups are always scoped by memberId, so another member's video is simply not found
    private ListeningVideo find(Long memberId, String videoId) {
        return listeningVideoRepository.findByMemberIdAndVideoId(memberId, toVideoId(videoId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리스닝 영상을 찾을 수 없습니다."));
    }

    private YoutubeVideoId toVideoId(String videoId) {
        try {
            return new YoutubeVideoId(videoId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
