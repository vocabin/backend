package com.vocabin.application.service;

import com.vocabin.application.port.out.ImportedClassRepository;
import com.vocabin.application.port.out.MemberRepository;
import com.vocabin.common.port.ClockHolder;
import com.vocabin.domain.autoimport.DiaryExpressionParser;
import com.vocabin.domain.autoimport.ImportedClass;
import com.vocabin.domain.member.Member;
import com.vocabin.domain.upload.ParsedWord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// Imports the recommended expressions of English-conversation diaries into the member's pocket.
// Enabled only for the single account configured in davidsenglish.pocket-import-email (deny by default).
@Slf4j
@Service
public class PocketExpressionImporter {

    static final LocalDate IMPORT_START_DATE = LocalDate.of(2026, 11, 1);
    static final String EXTERNAL_ID_PREFIX = "diary:";

    public record DiarySummary(String id, String classDate) {}

    private final ImportedClassRepository importedClassRepository;
    private final PocketService pocketService;
    private final MemberRepository memberRepository;
    private final ClockHolder clockHolder;
    private final String allowedEmail;

    public PocketExpressionImporter(
            ImportedClassRepository importedClassRepository,
            PocketService pocketService,
            MemberRepository memberRepository,
            ClockHolder clockHolder,
            @Value("${davidsenglish.pocket-import-email:}") String allowedEmail) {
        this.importedClassRepository = importedClassRepository;
        this.pocketService = pocketService;
        this.memberRepository = memberRepository;
        this.clockHolder = clockHolder;
        this.allowedEmail = allowedEmail;
    }

    /**
     * @param diaryLoader      fetches diary summaries; only called when the member is allowed
     * @param expressionLoader fetches the expressions text of a diary, or null when it is unavailable (retried next run)
     * @return number of diaries imported
     */
    @Transactional
    public int importNewDiaries(Long memberId,
                                Supplier<List<DiarySummary>> diaryLoader,
                                Function<String, String> expressionLoader) {
        if (!isAllowed(memberId)) {
            return 0;
        }

        List<DiarySummary> diaries = diaryLoader.get();
        if (diaries == null) {
            return 0;
        }

        Long pocketSetId = null;
        int imported = 0;
        for (DiarySummary diary : diaries) {
            if (!isOnOrAfterStartDate(diary.classDate())) continue;

            String externalId = EXTERNAL_ID_PREFIX + diary.id();
            if (importedClassRepository.existsByMemberIdAndExternalClassId(memberId, externalId)) continue;

            String expressions = expressionLoader.apply(diary.id());
            if (expressions == null) continue;

            if (pocketSetId == null) {
                pocketSetId = pocketService.ensurePocketSetId(memberId);
            }

            List<ParsedWord> parsed = DiaryExpressionParser.parse(expressions);
            if (parsed.isEmpty()) {
                log.warn("Pocket import: no parsable expressions in diary {}", diary.id());
            }
            String memo = diary.classDate() + " 수업";
            for (ParsedWord word : parsed) {
                pocketService.addIfAbsent(memberId, word.english(), word.korean(), memo);
            }

            importedClassRepository.save(ImportedClass.create(memberId, externalId, pocketSetId, clockHolder.now()));
            imported++;
            log.info("Pocket import: imported diary {} ({} expressions)", diary.id(), parsed.size());
        }
        return imported;
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

    private boolean isOnOrAfterStartDate(String classDate) {
        try {
            return !LocalDate.parse(classDate).isBefore(IMPORT_START_DATE);
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
    }
}
