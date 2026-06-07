package com.vocabin.infrastructure.autoimport;

import com.vocabin.application.port.out.AutoImportConfigRepository;
import com.vocabin.application.service.AutoImportService;
import com.vocabin.domain.autoimport.AutoImportConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoImportScheduler {

    private final AutoImportConfigRepository configRepository;
    private final AutoImportService autoImportService;

    // Run every hour at minute 0
    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        // DayOfWeek: MONDAY=1 ... SUNDAY=7
        int todayDow = now.getDayOfWeek().getValue();
        int currentHour = now.getHour();

        List<AutoImportConfig> configs = configRepository.findAllEnabled();
        for (AutoImportConfig config : configs) {
            if (config.getDayOfWeek() == todayDow && config.getHour() == currentHour) {
                log.info("Auto-import: triggering for memberId={}", config.getMemberId());
                try {
                    int count = autoImportService.fetchAndImport(config.getMemberId());
                    log.info("Auto-import: imported {} new class(es) for memberId={}", count, config.getMemberId());
                } catch (Exception e) {
                    log.error("Auto-import: error for memberId={}", config.getMemberId(), e);
                }
            }
        }
    }
}
