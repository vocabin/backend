package com.vocabin.api.listening;

import com.vocabin.domain.listening.VideoTranscript;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "영상 영어 자막")
public record TranscriptResponse(
        @Schema(description = "영어 자막이 있는지") boolean available,
        @Schema(description = "자막 언어 코드 (없으면 null)", example = "en") String language,
        @Schema(description = "유튜브 자동 생성 자막인지") boolean generated,
        @Schema(description = "자막 줄 목록 (시간순, 유튜브 원본 2~3초 단위)") List<Line> lines,
        @Schema(description = "문장 단위로 다시 묶은 자막 (시간순)") List<Sentence> sentences
) {
    @Schema(description = "자막 문장")
    public record Sentence(
            @Schema(description = "문장 시작 시간(초)", example = "426.1") double start,
            @Schema(description = "문장 끝 시간(초)", example = "430.4") double end,
            @Schema(description = "문장") String text
    ) {}

    @Schema(description = "자막 한 줄")
    public record Line(
            @Schema(description = "시작 시간(초)", example = "22.64") double start,
            @Schema(description = "표시 시간(초)", example = "4.32") double duration,
            @Schema(description = "자막 문장") String text
    ) {}

    public static TranscriptResponse from(VideoTranscript transcript) {
        return new TranscriptResponse(
                transcript.isAvailable(),
                transcript.getLanguage(),
                transcript.isGenerated(),
                transcript.getLines().stream().map(l -> new Line(l.start(), l.duration(), l.text())).toList(),
                transcript.sentences().stream().map(s -> new Sentence(s.start(), s.end(), s.text())).toList());
    }
}
