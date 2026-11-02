package com.vocabin.application.service;

import com.vocabin.application.port.out.AiExplainLogRepository;
import com.vocabin.application.port.out.ExpressionExtractor;
import com.vocabin.application.port.out.MemberRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.ai.AiExplainLog;
import com.vocabin.domain.ai.EnglishSentence;
import com.vocabin.domain.ai.ExpressionSuggestion;
import com.vocabin.domain.member.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

// Paid AI calls are guarded by: allowed account only, per-sentence cache, and a daily call limit
@Service
public class AiExpressionServiceImpl implements AiExpressionService {

    private final ExpressionExtractor expressionExtractor;
    private final AiExplainLogRepository logRepository;
    private final MemberRepository memberRepository;
    private final ClockHolder clockHolder;
    private final String allowedEmail;
    private final int dailyLimit;

    public AiExpressionServiceImpl(
            ExpressionExtractor expressionExtractor,
            AiExplainLogRepository logRepository,
            MemberRepository memberRepository,
            ClockHolder clockHolder,
            @Value("${openai.allowed-email:}") String allowedEmail,
            @Value("${openai.daily-limit:30}") int dailyLimit) {
        this.expressionExtractor = expressionExtractor;
        this.logRepository = logRepository;
        this.memberRepository = memberRepository;
        this.clockHolder = clockHolder;
        this.allowedEmail = allowedEmail;
        this.dailyLimit = dailyLimit;
    }

    @Override
    @Transactional
    public Result explain(Long memberId, String sentenceText) {
        if (!isAllowed(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AI 기능을 사용할 수 없는 계정입니다.");
        }
        EnglishSentence sentence = toSentence(sentenceText);

        Optional<AiExplainLog> cached = logRepository.findLatestByMemberIdAndSentenceHash(memberId, sentence.cacheKey());
        if (cached.isPresent()) {
            return new Result(sentence.value(), cached.get().getSuggestions(), true, remainingToday(memberId));
        }

        if (remainingToday(memberId) <= 0) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "오늘 AI 사용 횟수를 모두 사용했습니다.");
        }

        List<ExpressionSuggestion> suggestions;
        try {
            suggestions = expressionExtractor.extract(sentence);
        } catch (ExpressionExtractor.ExpressionExtractionException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 응답을 받지 못했습니다.");
        }

        logRepository.save(AiExplainLog.record(memberId, sentence, suggestions, clockHolder));
        return new Result(sentence.value(), suggestions, false, remainingToday(memberId));
    }

    private int remainingToday(Long memberId) {
        long used = logRepository.countByMemberIdSince(memberId, clockHolder.today().atStartOfDay());
        return (int) Math.max(0, dailyLimit - used);
    }

    private boolean isAllowed(Long memberId) {
        if (allowedEmail == null || allowedEmail.isBlank()) {
            return false;
        }
        return memberRepository.findById(memberId)
                .map(Member::getEmail)
                .filter(email -> email.equalsIgnoreCase(allowedEmail.trim()))
                .isPresent();
    }

    private EnglishSentence toSentence(String text) {
        try {
            return new EnglishSentence(text);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
