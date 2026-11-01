package com.vocabin.mock;

import com.vocabin.application.port.out.ListeningVideoRepository;
import com.vocabin.domain.listening.ListeningVideo;
import com.vocabin.domain.listening.YoutubeVideoId;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeListeningVideoRepository implements ListeningVideoRepository {

    private final Map<Long, ListeningVideo> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public List<ListeningVideo> findAllByMemberId(Long memberId) {
        return store.values().stream()
                .filter(video -> video.getMemberId().equals(memberId))
                .sorted(Comparator.comparing(ListeningVideo::getUpdatedAt).reversed())
                .toList();
    }

    @Override
    public Optional<ListeningVideo> findByMemberIdAndVideoId(Long memberId, YoutubeVideoId videoId) {
        return store.values().stream()
                .filter(video -> video.getMemberId().equals(memberId) && video.getVideoId().equals(videoId))
                .findFirst();
    }

    @Override
    public ListeningVideo save(ListeningVideo video) {
        Long id = video.getId() != null ? video.getId() : idGenerator.incrementAndGet();
        ListeningVideo saved = ListeningVideo.builder()
                .id(id)
                .memberId(video.getMemberId())
                .videoId(video.getVideoId())
                .title(video.getTitle())
                .lastPositionSeconds(video.getLastPositionSeconds())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
        store.put(id, saved);
        return saved;
    }

    @Override
    public void delete(ListeningVideo video) {
        store.remove(video.getId());
    }
}
