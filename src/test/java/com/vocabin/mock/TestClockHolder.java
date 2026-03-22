package com.vocabin.mock;

import com.vocabin.common.port.ClockHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestClockHolder implements ClockHolder {

    private final LocalDateTime fixedDateTime;

    public TestClockHolder(LocalDateTime fixedDateTime) {
        this.fixedDateTime = fixedDateTime;
    }

    @Override
    public LocalDateTime now() {
        return fixedDateTime;
    }

    @Override
    public LocalDate today() {
        return fixedDateTime.toLocalDate();
    }
}
