package com.vocabin.application.port.out;

import com.vocabin.domain.listening.ListeningVideo;
import com.vocabin.domain.listening.YoutubeVideoId;

import java.util.List;
import java.util.Optional;

public interface ListeningVideoRepository {
    List<ListeningVideo> findAllByMemberId(Long memberId);
    Optional<ListeningVideo> findByMemberIdAndVideoId(Long memberId, YoutubeVideoId videoId);
    ListeningVideo save(ListeningVideo listeningVideo);
    void delete(ListeningVideo listeningVideo);
}
