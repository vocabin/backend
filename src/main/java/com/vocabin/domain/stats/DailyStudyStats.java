package com.vocabin.domain.stats;

import java.time.LocalDate;

public record DailyStudyStats(LocalDate date, int total, int correct) {
    public double correctRate() {
        if (total == 0) return 0.0;
        return (double) correct / total;
    }
}
