package com.market.scale.credit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class TimeDecay {

    private TimeDecay() {}

    public static double decayFactor(LocalDateTime occurredAt, LocalDateTime now, int decayMonths) {
        if (decayMonths <= 0) return 1.0;
        long monthsBetween = ChronoUnit.MONTHS.between(occurredAt, now);
        if (monthsBetween <= 0) return 1.0;
        if (monthsBetween >= decayMonths) return 0.0;
        return 1.0 - (double) monthsBetween / decayMonths;
    }

    public static int applyDecay(int rawDelta, LocalDateTime occurredAt, LocalDateTime now, int decayMonths, boolean isPenalty) {
        if (!isPenalty || decayMonths <= 0) return rawDelta;
        double factor = decayFactor(occurredAt, now, decayMonths);
        return (int) Math.round(rawDelta * factor);
    }
}
