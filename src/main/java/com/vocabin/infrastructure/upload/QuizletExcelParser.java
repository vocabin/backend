package com.vocabin.infrastructure.upload;

import com.vocabin.application.port.out.FileParser;
import com.vocabin.domain.upload.ParsedWord;
import com.vocabin.domain.upload.UploadType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuizletExcelParser implements FileParser {

    @Override
    public boolean supports(UploadType type, String extension) {
        return type == UploadType.QUIZLET
                && ("xlsx".equalsIgnoreCase(extension) || "xls".equalsIgnoreCase(extension));
    }

    /**
     * Quizlet Excel export format:
     *   col A = english, col B = korean (no header row)
     */
    @Override
    public List<ParsedWord> parse(MultipartFile file) throws IOException {
        List<ParsedWord> result = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell englishCell = row.getCell(0);
                Cell koreanCell = row.getCell(1);
                if (englishCell == null || koreanCell == null) continue;

                String english = englishCell.getStringCellValue().trim();
                String korean = koreanCell.getStringCellValue().trim();
                if (!english.isBlank() && !korean.isBlank()) {
                    result.add(new ParsedWord(english, korean));
                }
            }
        }

        return result;
    }
}
