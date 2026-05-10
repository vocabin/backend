package com.vocabin.application.service;

import com.vocabin.domain.upload.UploadResult;
import com.vocabin.domain.upload.UploadType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WordUploadService {
    UploadResult upload(Long wordSetId, UploadType type, MultipartFile file) throws IOException;
}
