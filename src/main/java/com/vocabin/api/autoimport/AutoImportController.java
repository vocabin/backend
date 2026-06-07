package com.vocabin.api.autoimport;

import com.vocabin.application.service.AutoImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Auto Import", description = "퀴즐렛 자동 임포트 설정 API")
@RestController
@RequestMapping("/api/auto-import")
@RequiredArgsConstructor
public class AutoImportController {

    private final AutoImportService autoImportService;

    @Operation(summary = "자동 임포트 설정 조회")
    @GetMapping("/config")
    public ResponseEntity<AutoImportConfigResponse> getConfig(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        return ResponseEntity.ok(AutoImportConfigResponse.from(autoImportService.getConfig(memberId)));
    }

    @Operation(summary = "자동 임포트 설정 수정")
    @PutMapping("/config")
    public ResponseEntity<AutoImportConfigResponse> updateConfig(
            HttpServletRequest request,
            @Valid @RequestBody UpdateAutoImportConfigRequest body) {
        Long memberId = (Long) request.getAttribute("memberId");
        var updated = autoImportService.updateConfig(memberId, body.dayOfWeek(), body.hour(), body.enabled());
        return ResponseEntity.ok(AutoImportConfigResponse.from(updated));
    }

    @Operation(summary = "지금 바로 임포트 실행")
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Integer>> trigger(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        int count = autoImportService.fetchAndImport(memberId);
        return ResponseEntity.ok(Map.of("imported", count));
    }

    @Operation(summary = "임포트 이력 조회")
    @GetMapping("/history")
    public ResponseEntity<List<ImportHistoryResponse>> history(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        var list = autoImportService.getHistory(memberId)
                .stream()
                .map(ImportHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }
}
