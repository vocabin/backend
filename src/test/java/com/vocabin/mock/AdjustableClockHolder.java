package com.vocabin.mock;

import com.vocabin.common.port.ClockHolder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Test clock that can be moved forward to verify time-ordered behavior
public class AdjustableClockHolder implements ClockHolder {

    private LocalDateTime current;

    public AdjustableClockHolder(LocalDateTime start) {
        this.current = start;
    }

    public void advance(Duration duration) {
        current = current.plus(duration);
    }

    @Override
    public LocalDateTime now() {
        return current;
    }

    @Override
    public LocalDate today() {
        return current.toLocalDate();
    }
}
