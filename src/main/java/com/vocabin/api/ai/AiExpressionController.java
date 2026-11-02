package com.vocabin.api.ai;

import com.vocabin.application.service.AiExpressionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "AI 표현 추출 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiExpressionController {

    private final AiExpressionService aiExpressionService;

    @Operation(summary = "문장에서 표현 뽑기",
            description = "문장 하나만 AI에 보내 숙어/표현/시제를 뽑습니다. 같은 문장은 저장된 결과를 재사용하고, 허용된 계정만 하루 한도 내에서 호출됩니다. (403: 비허용 계정, 429: 한도 초과, 503: AI 응답 실패)")
    @PostMapping("/expressions")
    public ExplainResponse explain(
            @RequestAttribute("memberId") Long memberId,
            @RequestBody @Valid ExplainRequest request) {
        return ExplainResponse.from(aiExpressionService.explain(memberId, request.sentence()));
    }
}
