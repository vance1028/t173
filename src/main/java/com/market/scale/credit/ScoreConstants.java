package com.market.scale.credit;

public final class ScoreConstants {

    public static final int BASE_SCORE = 100;
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 120;

    private ScoreConstants() {}

    public static int clamp(int score) {
        if (score < MIN_SCORE) return MIN_SCORE;
        if (score > MAX_SCORE) return MAX_SCORE;
        return score;
    }
}
