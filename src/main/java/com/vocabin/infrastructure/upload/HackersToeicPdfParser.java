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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 해커스 토익 800+ 단어암기장 PDF 파서.
 *
 * 표 형식: NUM | 영단어/구 | 한국어 뜻 | NUM | 영단어/구 | 한국어 뜻 (1–25 / 26–50)
 *
 * PDFTextStripper는 위치 기반으로 텍스트를 추출하므로
 * 한 행에 두 항목이 연속으로 나타날 수 있습니다.
 * 예) "1 look in ~을 들여다보다   26 climb down ~을 내려가다"
 *
 * 파싱 전략:
 *  - 숫자 + 영어(라틴) + 한국어 의미 패턴을 매칭
 *  - 한국어 의미는 한글 또는 품사 약어(n. v. adj. adv. prep.) + 한글로 시작
 *  - 다음 숫자 항목 혹은 줄바꿈으로 경계 판별
 */
@Component
public class HackersToeicPdfParser implements FileParser {

    // Meaning starts with: Korean char, ~, (, or part-of-speech abbreviation
    private static final String MEANING_START =
            "(?:(?:n|v|adj|adv|prep)\\.\\s+)?[가-힣~(\\[]";

    // Full entry pattern
    private static final Pattern ENTRY = Pattern.compile(
            "(\\d{1,2})\\s+" +                              // number (1-50)
            "([A-Za-z][A-Za-z /.,''()\\-]*?)" +              // english phrase (lazy)
            "\\s+" +                                         // whitespace separator
            "(" + MEANING_START + "[^\r\n0-9]*)" +           // korean meaning (no digits, no newline)
            "(?=\\s*\\d{1,2}\\s+[A-Za-z]|\\s*[\\r\\n]|\\s*$)",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    @Override
    public boolean supports(UploadType type, String extension) {
        return type == UploadType.HACKERS_TOEIC && "pdf".equalsIgnoreCase(extension);
    }

    @Override
    public List<ParsedWord> parse(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return parseText(text);
        }
    }

    List<ParsedWord> parseText(String text) {
        List<ParsedWord> result = new ArrayList<>();
        // Normalize: collapse multiple spaces into two (preserve column boundaries)
        String normalized = text.replaceAll("[ \\t]{3,}", "   ");

        Matcher m = ENTRY.matcher(normalized);
        while (m.find()) {
            String english = m.group(2).trim().replaceAll("\\s+", " ");
            String korean  = m.group(3).trim().replaceAll("\\s+", " ").replaceAll(",$", "");
            if (!english.isBlank() && !korean.isBlank()) {
                result.add(new ParsedWord(english, korean));
            }
        }
        return result;
    }
}
