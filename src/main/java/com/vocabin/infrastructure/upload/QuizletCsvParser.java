package com.vocabin.infrastructure.upload;

import com.vocabin.application.port.out.FileParser;
import com.vocabin.domain.upload.ParsedWord;
import com.vocabin.domain.upload.UploadType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuizletCsvParser implements FileParser {

    @Override
    public boolean supports(UploadType type, String extension) {
        return type == UploadType.QUIZLET && "csv".equalsIgnoreCase(extension);
    }

    /**
     * Quizlet CSV export format: tab-separated
     *   english\tkorean
     *   (no header row)
     */
    @Override
    public List<ParsedWord> parse(MultipartFile file) throws IOException {
        List<ParsedWord> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                String[] parts = trimmed.split("\t", 2);
                if (parts.length < 2) continue;

                String english = parts[0].trim();
                String korean = parts[1].trim();
                if (!english.isBlank() && !korean.isBlank()) {
                    result.add(new ParsedWord(english, korean));
                }
            }
        }

        return result;
    }
}
