package com.vocabin.application.service;

import com.vocabin.application.port.out.FileParser;
import com.vocabin.application.port.out.WordRepository;
import com.vocabin.application.port.out.WordSetRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.upload.ParsedWord;
import com.vocabin.domain.upload.UploadResult;
import com.vocabin.domain.upload.UploadType;
import com.vocabin.domain.word.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WordUploadServiceImpl implements WordUploadService {

    private final List<FileParser> parsers;
    private final WordRepository wordRepository;
    private final WordSetRepository wordSetRepository;
    private final ClockHolder clockHolder;

    @Override
    @Transactional
    public UploadResult upload(Long wordSetId, UploadType type, MultipartFile file) throws IOException {
        wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new NoSuchElementException("단어 세트를 찾을 수 없습니다. id=" + wordSetId));

        String extension = extractExtension(file.getOriginalFilename());

        FileParser parser = parsers.stream()
                .filter(p -> p.supports(type, extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 파일 형식입니다: type=" + type + ", extension=" + extension));

        List<ParsedWord> parsed = parser.parse(file);

        for (ParsedWord pw : parsed) {
            wordRepository.save(Word.create(wordSetId, pw.english(), pw.korean(), clockHolder));
        }

        return new UploadResult(parsed.size());
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
