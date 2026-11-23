package com.vocabin.infrastructure.listening;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.VideoTranscriptRepository;
import com.vocabin.domain.listening.TranscriptLine;
import com.vocabin.domain.listening.VideoTranscript;
import com.vocabin.domain.listening.YoutubeVideoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VideoTranscriptRepositoryImpl implements VideoTranscriptRepository {

    private static final TypeReference<List<TranscriptLine>> LINES = new TypeReference<>() {};

    private final VideoTranscriptJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<VideoTranscript> findByVideoId(YoutubeVideoId videoId) {
        return jpaRepository.findByVideoId(videoId.value()).map(this::toModel);
    }

    @Override
    public VideoTranscript save(VideoTranscript transcript) {
        try {
            VideoTranscriptEntity saved = jpaRepository.save(VideoTranscriptEntity.of(
                    transcript.getId(), transcript.getVideoId().value(), transcript.getLanguage(), transcript.isGenerated(),
                    objectMapper.writeValueAsString(transcript.getLines()), transcript.getFetchedAt()));
            return toModel(saved);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transcript", e);
        }
    }

    private VideoTranscript toModel(VideoTranscriptEntity entity) {
        try {
            return VideoTranscript.builder()
                    .id(entity.getId())
                    .videoId(new YoutubeVideoId(entity.getVideoId()))
                    .language(entity.getLanguage())
                    .generated(entity.isGenerated())
                    .lines(objectMapper.readValue(entity.getLinesJson(), LINES))
                    .fetchedAt(entity.getFetchedAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot read stored transcript", e);
        }
    }
}
