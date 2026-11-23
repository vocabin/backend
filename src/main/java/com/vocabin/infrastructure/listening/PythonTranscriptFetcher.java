package com.vocabin.infrastructure.listening;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabin.application.port.out.TranscriptFetcher;
import com.vocabin.domain.listening.TranscriptLine;
import com.vocabin.domain.listening.YoutubeVideoId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Runs the youtube-transcript-api Python library (installed in the backend image) and reads its JSON output.
// The library is kept outside the JVM so it can be upgraded on its own when YouTube changes.
@Slf4j
@Component
public class PythonTranscriptFetcher implements TranscriptFetcher {

    private static final long TIMEOUT_SECONDS = 20;

    // Manually created English captions first, then auto-generated ones
    static final String SCRIPT = """
            import json, sys
            from youtube_transcript_api import YouTubeTranscriptApi
            from youtube_transcript_api._errors import NoTranscriptFound, TranscriptsDisabled
            try:
                transcripts = YouTubeTranscriptApi().list(sys.argv[1])
            except TranscriptsDisabled:
                print(json.dumps({"status": "none"})); sys.exit(0)
            chosen = None
            for find in (transcripts.find_manually_created_transcript, transcripts.find_generated_transcript):
                try:
                    chosen = find(["en", "en-US", "en-GB"]); break
                except NoTranscriptFound:
                    pass
            if chosen is None:
                print(json.dumps({"status": "none"})); sys.exit(0)
            fetched = chosen.fetch()
            print(json.dumps({"status": "ok", "language": chosen.language_code, "generated": chosen.is_generated,
                              "lines": [{"text": s.text, "start": s.start, "duration": s.duration} for s in fetched.snippets]}))
            """;

    private final ObjectMapper objectMapper;
    private final String pythonCommand;

    public PythonTranscriptFetcher(ObjectMapper objectMapper,
                                   @Value("${transcript.python-command:python3}") String pythonCommand) {
        this.objectMapper = objectMapper;
        this.pythonCommand = pythonCommand;
    }

    @Override
    public Optional<FetchedTranscript> fetchEnglish(YoutubeVideoId videoId) {
        String output = run(videoId);
        return parse(output);
    }

    private String run(YoutubeVideoId videoId) {
        Process process = null;
        try {
            // The video id is a validated 11-char value object and is passed as an argument, never as shell text
            process = new ProcessBuilder(pythonCommand, "-c", SCRIPT, videoId.value()).start();
            CompletableFuture<String> stdout = readAsync(process.getInputStream());
            CompletableFuture<String> stderr = readAsync(process.getErrorStream());
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new TranscriptFetchException("Transcript fetch timed out for " + videoId.value(), null);
            }
            if (process.exitValue() != 0) {
                String error = stderr.get(1, TimeUnit.SECONDS);
                log.warn("Transcript fetch failed for {}: {}", videoId.value(), lastLine(error));
                throw new TranscriptFetchException("Transcript fetch failed for " + videoId.value(), null);
            }
            return stdout.get(1, TimeUnit.SECONDS);
        } catch (TranscriptFetchException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new TranscriptFetchException("Transcript fetch failed for " + videoId.value(), e);
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }

    Optional<FetchedTranscript> parse(String output) {
        try {
            JsonNode root = objectMapper.readTree(output);
            if (!"ok".equals(root.path("status").asText())) {
                return Optional.empty();
            }
            List<TranscriptLine> lines = new ArrayList<>();
            for (JsonNode line : root.path("lines")) {
                lines.add(new TranscriptLine(line.path("start").asDouble(), line.path("duration").asDouble(), line.path("text").asText("")));
            }
            return Optional.of(new FetchedTranscript(root.path("language").asText(null), root.path("generated").asBoolean(false), lines));
        } catch (Exception e) {
            throw new TranscriptFetchException("Cannot parse transcript output", e);
        }
    }

    private static CompletableFuture<String> readAsync(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return "";
            }
        });
    }

    private static String lastLine(String text) {
        String trimmed = text.strip();
        int index = trimmed.lastIndexOf('\n');
        return index >= 0 ? trimmed.substring(index + 1) : trimmed;
    }
}
