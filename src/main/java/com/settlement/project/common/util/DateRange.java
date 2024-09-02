package com.settlement.project.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateRange {
    private final LocalDate start;
    private final LocalDate end;

    private DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end.isAfter(LocalDate.now()) ? LocalDate.now() : end;
    }

    public static DateRange of(String period, LocalDate now) {
        LocalDate start;
        LocalDate end;

        switch (period.toLowerCase()) {
            case "daily":
                start = now;
                end = now;
                break;
            case "weekly":
                start = now.with(DayOfWeek.MONDAY);
                end = now.with(DayOfWeek.SUNDAY);
                break;
            case "monthly":
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }

        return new DateRange(start, end);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
