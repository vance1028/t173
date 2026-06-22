package com.market.scale.credit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CreditScoringEngine {

    private CreditScoringEngine() {}

    public static ScoreChange applyEvent(String ruleCode, String severity, Integer magnitudeValue,
                                         LocalDateTime occurredAt, int currentScore, LocalDateTime now) {
        ScoringRule rule = ScoringRules.get(ruleCode);
        if (rule == null) {
            throw new IllegalArgumentException("未知评分规则: " + ruleCode);
        }
        return new ScoreChange(rule, severity, magnitudeValue, occurredAt, currentScore, now);
    }

    public static int recalculateFromHistory(int baseScore, List<ScoreChange> history, LocalDateTime now) {
        int score = ScoreConstants.clamp(baseScore);
        List<ScoreChange> sorted = new ArrayList<>(history);
        sorted.sort(Comparator.comparing(ScoreChange::getOccurredAt));
        for (ScoreChange change : sorted) {
            ScoringRule rule = change.getRule();
            int raw = rule.computeRawDelta(change.getSeverity(), change.getMagnitudeValue());
            int decayed = TimeDecay.applyDecay(raw, change.getOccurredAt(), now,
                rule.getDecayMonths(), rule.isPenalty());
            int tentative = score + decayed;
            int clamped = ScoreConstants.clamp(tentative);
            score = clamped;
        }
        return score;
    }

    public static int recommendPatrolFrequency(CreditLevel level, int baseFrequency) {
        return (int) Math.ceil(baseFrequency * level.getPatrolMultiplier());
    }

    public static boolean eligibleForRedList(int score, int cleanDays) {
        return score >= CreditLevel.EXCELLENT.getMinScore() && cleanDays >= 30;
    }

    public static boolean eligibleForBlackList(int score) {
        return score < CreditLevel.SERIOUS.getMaxScore();
    }
}
