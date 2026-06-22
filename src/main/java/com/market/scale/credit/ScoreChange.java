package com.market.scale.credit;

import java.time.LocalDateTime;

public class ScoreChange {

    private final ScoringRule rule;
    private final String severity;
    private final Integer magnitudeValue;
    private final LocalDateTime occurredAt;
    private final int scoreBefore;
    private final int rawDelta;
    private final int decayedDelta;
    private final int appliedDelta;
    private final int scoreAfter;

    public ScoreChange(ScoringRule rule, String severity, Integer magnitudeValue,
                       LocalDateTime occurredAt, int scoreBefore, LocalDateTime now) {
        this.rule = rule;
        this.severity = severity;
        this.magnitudeValue = magnitudeValue;
        this.occurredAt = occurredAt;
        this.scoreBefore = ScoreConstants.clamp(scoreBefore);
        this.rawDelta = rule.computeRawDelta(severity, magnitudeValue);
        this.decayedDelta = TimeDecay.applyDecay(rawDelta, occurredAt, now,
            rule.getDecayMonths(), rule.isPenalty());
        int tentative = this.scoreBefore + this.decayedDelta;
        int clamped = ScoreConstants.clamp(tentative);
        this.appliedDelta = clamped - this.scoreBefore;
        this.scoreAfter = clamped;
    }

    public ScoringRule getRule() { return rule; }
    public String getSeverity() { return severity; }
    public Integer getMagnitudeValue() { return magnitudeValue; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public int getScoreBefore() { return scoreBefore; }
    public int getRawDelta() { return rawDelta; }
    public int getDecayedDelta() { return decayedDelta; }
    public int getAppliedDelta() { return appliedDelta; }
    public int getScoreAfter() { return scoreAfter; }

    public boolean isAddition() { return rawDelta >= 0; }
}
