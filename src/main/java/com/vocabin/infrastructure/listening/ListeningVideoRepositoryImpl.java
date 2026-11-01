package com.vocabin.infrastructure.listening;

import com.vocabin.application.port.out.ListeningVideoRepository;
import com.vocabin.domain.listening.ListeningVideo;
import com.vocabin.domain.listening.YoutubeVideoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ListeningVideoRepositoryImpl implements ListeningVideoRepository {

    private final ListeningVideoJpaRepository jpaRepository;

    @Override
    public List<ListeningVideo> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberIdOrderByUpdatedAtDesc(memberId).stream()
                .map(ListeningVideoEntity::toModel)
                .toList();
    }

    @Override
    public Optional<ListeningVideo> findByMemberIdAndVideoId(Long memberId, YoutubeVideoId videoId) {
        return jpaRepository.findByMemberIdAndVideoId(memberId, videoId.value())
                .map(ListeningVideoEntity::toModel);
    }

    @Override
    public ListeningVideo save(ListeningVideo listeningVideo) {
        return jpaRepository.save(ListeningVideoEntity.from(listeningVideo)).toModel();
    }

    @Override
    public void delete(ListeningVideo listeningVideo) {
        jpaRepository.deleteById(listeningVideo.getId());
    }
}
