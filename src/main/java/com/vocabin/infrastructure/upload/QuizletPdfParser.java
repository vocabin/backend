package com.vocabin.infrastructure.upload;

import com.vocabin.application.port.out.FileParser;
import com.vocabin.domain.upload.ParsedWord;
import com.vocabin.domain.upload.UploadType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuizletPdfParser implements FileParser {

    @Override
    public boolean supports(UploadType type, String extension) {
        return type == UploadType.QUIZLET && "pdf".equalsIgnoreCase(extension);
    }

    /**
     * Quizlet PDF format:
     *   1. English sentence (may wrap to next line)
     *      → Korean translation
     */
    @Override
    public List<ParsedWord> parse(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return parseText(text);
        }
    }

    private List<ParsedWord> parseText(String text) {
        List<ParsedWord> result = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");

        String currentEnglish = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.matches("^\\d+\\.\\s+.*")) {
                // new entry: strip the leading "N. "
                currentEnglish = trimmed.replaceFirst("^\\d+\\.\\s+", "");

            } else if (trimmed.startsWith("→") && currentEnglish != null) {
                String korean = trimmed.replaceFirst("^→\\s*", "").trim();
                if (!currentEnglish.isBlank() && !korean.isBlank()) {
                    result.add(new ParsedWord(currentEnglish.trim(), korean));
                }
                currentEnglish = null;

            } else if (currentEnglish != null) {
                // continuation of multi-line English
                currentEnglish = currentEnglish + " " + trimmed;
            }
        }

        return result;
    }
}
