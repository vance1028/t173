package com.market.scale.credit;

@FunctionalInterface
public interface MagnitudeMultiplier {

    double apply(int magnitudeValue);

    static MagnitudeMultiplier shortagePercent() {
        return magnitudeValue -> {
            if (magnitudeValue <= 0) return 0.0;
            double pct = magnitudeValue / 100.0;
            if (pct < 0.05) return 0.5;
            if (pct < 0.10) return 0.8;
            if (pct < 0.20) return 1.0;
            if (pct < 0.30) return 1.5;
            return 2.0;
        };
    }

    static MagnitudeMultiplier expiredDays() {
        return magnitudeValue -> {
            if (magnitudeValue <= 0) return 0.0;
            if (magnitudeValue <= 7) return 0.8;
            if (magnitudeValue <= 30) return 1.0;
            if (magnitudeValue <= 90) return 1.3;
            return 1.6;
        };
    }

    static MagnitudeMultiplier none() {
        return v -> 1.0;
    }
}
