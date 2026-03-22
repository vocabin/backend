package com.vocabin.common.port;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ClockHolder {
    LocalDateTime now();
    LocalDate today();
}
