package com.market.scale.credit;

public enum CreditLevel {

    EXCELLENT("excellent", "优秀", 110, Integer.MAX_VALUE, 0.3),
    GOOD("good", "良好", 90, 110, 0.7),
    AVERAGE("average", "一般", 70, 90, 1.0),
    POOR("poor", "较差", 40, 70, 1.5),
    SERIOUS("serious", "严重失信", Integer.MIN_VALUE, 40, 2.5);

    private final String code;
    private final String label;
    private final int minScore;
    private final int maxScore;
    private final double patrolMultiplier;

    CreditLevel(String code, String label, int minScore, int maxScore, double patrolMultiplier) {
        this.code = code;
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.patrolMultiplier = patrolMultiplier;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public int getMinScore() { return minScore; }
    public int getMaxScore() { return maxScore; }
    public double getPatrolMultiplier() { return patrolMultiplier; }

    public static CreditLevel fromScore(int score) {
        int s = ScoreConstants.clamp(score);
        for (CreditLevel level : values()) {
            if (s >= level.minScore && s < level.maxScore) {
                return level;
            }
        }
        return GOOD;
    }

    public static CreditLevel fromCode(String code) {
        if (code == null) return GOOD;
        for (CreditLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) return level;
        }
        return GOOD;
    }
}
