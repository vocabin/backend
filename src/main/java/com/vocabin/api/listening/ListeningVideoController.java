package com.vocabin.api.listening;

import com.vocabin.application.service.ListeningVideoService;
import com.vocabin.application.service.TranscriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Listening", description = "리스닝 노트 영상 목록 API")
@RestController
@RequestMapping("/api/listening-videos")
@RequiredArgsConstructor
public class ListeningVideoController {

    private final ListeningVideoService listeningVideoService;
    private final TranscriptService transcriptService;

    @Operation(summary = "리스닝 영상 목록 조회", description = "최근에 연 영상부터 반환합니다.")
    @GetMapping
    public List<ListeningVideoResponse> getVideos(@RequestAttribute("memberId") Long memberId) {
        return listeningVideoService.getVideos(memberId).stream()
                .map(ListeningVideoResponse::from)
                .toList();
    }

    @Operation(summary = "리스닝 영상 열기", description = "목록에 없으면 추가하고, 있으면 최근 순서와 제목을 갱신합니다.")
    @PutMapping("/{videoId}")
    public ListeningVideoResponse open(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable String videoId,
            @RequestBody(required = false) @Valid OpenListeningVideoRequest request) {
        String title = request != null ? request.title() : null;
        return ListeningVideoResponse.from(listeningVideoService.open(memberId, videoId, title));
    }

    @Operation(summary = "이어보기 위치 저장")
    @PatchMapping("/{videoId}/position")
    public ListeningVideoResponse savePosition(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable String videoId,
            @RequestBody @Valid SavePositionRequest request) {
        return ListeningVideoResponse.from(listeningVideoService.savePosition(memberId, videoId, request.seconds()));
    }

    @Operation(summary = "리스닝 영상 삭제", description = "목록에서만 지웁니다. 이 영상에서 담은 포켓 표현은 그대로 남습니다.")
    @DeleteMapping("/{videoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable String videoId) {
        listeningVideoService.remove(memberId, videoId);
    }

    @Operation(summary = "영상 영어 자막 조회",
            description = "영상마다 처음 한 번만 유튜브에서 영어 자막(수동 → 자동 생성 순)을 가져와 저장하고 이후엔 저장본을 반환합니다. 영어 자막이 없으면 available=false. (503: 가져오기 실패, 다음 요청 때 재시도)")
    @GetMapping("/{videoId}/transcript")
    public TranscriptResponse getTranscript(@PathVariable String videoId) {
        return TranscriptResponse.from(transcriptService.getTranscript(videoId));
    }
}
