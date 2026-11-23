package com.vocabin.domain.listening;

import com.vocabin.common.port.ClockHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

// English captions of a video, fetched once and reused. A transcript with no lines records
// "this video has no English captions" so YouTube is not asked again for it.
@Getter
@Builder
public class VideoTranscript {

    // Lines that are only sound cues, e.g. "[Music]", "[♪♪♪]", "♪"
    private static final Pattern SOUND_ONLY = Pattern.compile("^[\\s\\[\\]()♪♫]*(music|applause|laughter)?[\\s\\[\\]()♪♫]*$", Pattern.CASE_INSENSITIVE);

    private final Long id;
    private final YoutubeVideoId videoId;
    private final String language;
    private final boolean generated;
    private final List<TranscriptLine> lines;
    private final LocalDateTime fetchedAt;

    public static VideoTranscript of(YoutubeVideoId videoId, String language, boolean generated,
                                     List<TranscriptLine> rawLines, ClockHolder clockHolder) {
        List<TranscriptLine> cleaned = rawLines.stream()
                .map(line -> new TranscriptLine(line.start(), line.duration(), clean(line.text())))
                .filter(line -> !SOUND_ONLY.matcher(line.text()).matches())
                .toList();
        return VideoTranscript.builder()
                .videoId(videoId)
                .language(language)
                .generated(generated)
                .lines(cleaned)
                .fetchedAt(clockHolder.now())
                .build();
    }

    public static VideoTranscript unavailable(YoutubeVideoId videoId, ClockHolder clockHolder) {
        return VideoTranscript.builder()
                .videoId(videoId)
                .generated(false)
                .lines(List.of())
                .fetchedAt(clockHolder.now())
                .build();
    }

    public boolean isAvailable() {
        return !lines.isEmpty();
    }

    public List<TranscriptSentence> sentences() {
        return TranscriptSentences.from(lines);
    }

    // Captions wrap long sentences onto two lines; join them into one
    private static String clean(String text) {
        return text == null ? "" : text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }
}
