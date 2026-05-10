package com.vocabin.application.port.out;

import com.vocabin.domain.upload.ParsedWord;
import com.vocabin.domain.upload.UploadType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileParser {

    boolean supports(UploadType type, String extension);

    List<ParsedWord> parse(MultipartFile file) throws IOException;
}
