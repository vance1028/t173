package com.market.scale.credit;

public class ScoringRule {

    private final String ruleCode;
    private final String ruleName;
    private final int baseDelta;
    private final boolean hasSeverity;
    private final boolean hasMagnitude;
    private final int decayMonths;
    private final MagnitudeMultiplier magnitudeMultiplier;

    public ScoringRule(String ruleCode, String ruleName, int baseDelta,
                       boolean hasSeverity, boolean hasMagnitude, int decayMonths,
                       MagnitudeMultiplier magnitudeMultiplier) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.baseDelta = baseDelta;
        this.hasSeverity = hasSeverity;
        this.hasMagnitude = hasMagnitude;
        this.decayMonths = decayMonths;
        this.magnitudeMultiplier = magnitudeMultiplier == null ? MagnitudeMultiplier.none() : magnitudeMultiplier;
    }

    public String getRuleCode() { return ruleCode; }
    public String getRuleName() { return ruleName; }
    public int getBaseDelta() { return baseDelta; }
    public boolean isHasSeverity() { return hasSeverity; }
    public boolean isHasMagnitude() { return hasMagnitude; }
    public int getDecayMonths() { return decayMonths; }
    public boolean isPenalty() { return baseDelta < 0; }

    public int computeRawDelta(String severityCode, Integer magnitudeValue) {
        double result = baseDelta;
        if (hasSeverity) {
            result *= Severity.fromCode(severityCode).getMultiplier();
        }
        if (hasMagnitude && magnitudeValue != null) {
            result *= magnitudeMultiplier.apply(magnitudeValue);
        }
        return (int) Math.round(result);
    }

    public String toSnapshotJson() {
        return String.format(
            "{\"ruleCode\":\"%s\",\"ruleName\":\"%s\",\"baseDelta\":%d,\"hasSeverity\":%s,\"hasMagnitude\":%s,\"decayMonths\":%d}",
            ruleCode, ruleName, baseDelta, hasSeverity, hasMagnitude, decayMonths
        );
    }
}
