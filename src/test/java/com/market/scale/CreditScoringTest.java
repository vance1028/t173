package com.market.scale;

import com.market.scale.credit.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreditScoringTest {

    @Test
    void baseScoreAndBounds() {
        assertEquals(100, ScoreConstants.BASE_SCORE);
        assertEquals(0, ScoreConstants.MIN_SCORE);
        assertEquals(120, ScoreConstants.MAX_SCORE);
        assertEquals(50, ScoreConstants.clamp(50));
        assertEquals(0, ScoreConstants.clamp(-10));
        assertEquals(120, ScoreConstants.clamp(200));
    }

    @Test
    void creditLevelMapping() {
        assertEquals(CreditLevel.EXCELLENT, CreditLevel.fromScore(115));
        assertEquals(CreditLevel.GOOD, CreditLevel.fromScore(100));
        assertEquals(CreditLevel.GOOD, CreditLevel.fromScore(95));
        assertEquals(CreditLevel.AVERAGE, CreditLevel.fromScore(80));
        assertEquals(CreditLevel.POOR, CreditLevel.fromScore(50));
        assertEquals(CreditLevel.SERIOUS, CreditLevel.fromScore(30));
        assertEquals(CreditLevel.SERIOUS, CreditLevel.fromScore(0));
        assertEquals("优秀", CreditLevel.EXCELLENT.getLabel());
        assertEquals("严重失信", CreditLevel.SERIOUS.getLabel());
    }

    @Test
    void severityMultiplier() {
        assertEquals(0.5, Severity.MILD.getMultiplier());
        assertEquals(1.0, Severity.NORMAL.getMultiplier());
        assertEquals(2.0, Severity.SERIOUS.getMultiplier());
        assertEquals(Severity.NORMAL, Severity.fromCode(null));
        assertEquals(Severity.NORMAL, Severity.fromCode("unknown"));
        assertEquals(Severity.SERIOUS, Severity.fromCode("serious"));
    }

    @Test
    void shortageMagnitudeMultiplier() {
        MagnitudeMultiplier m = MagnitudeMultiplier.shortagePercent();
        assertEquals(0.0, m.apply(0));
        assertEquals(0.5, m.apply(3));
        assertEquals(0.8, m.apply(7));
        assertEquals(1.0, m.apply(15));
        assertEquals(1.5, m.apply(25));
        assertEquals(2.0, m.apply(50));
    }

    @Test
    void scoringRuleComputesRawDelta() {
        ScoringRule shortage = ScoringRules.get("RECHECK_SHORTAGE");
        assertNotNull(shortage);
        assertEquals(-10, shortage.getBaseDelta());
        assertTrue(shortage.isPenalty());
        assertEquals(12, shortage.getDecayMonths());

        int mildSmall = shortage.computeRawDelta("mild", 3);
        int normalMedium = shortage.computeRawDelta("normal", 15);
        int seriousLarge = shortage.computeRawDelta("serious", 25);

        assertTrue(mildSmall < 0);
        assertTrue(normalMedium < mildSmall);
        assertTrue(seriousLarge < normalMedium);
    }

    @Test
    void scoreChangeAppliesClamp() {
        LocalDateTime now = LocalDateTime.now();
        ScoreChange hitBottom = CreditScoringEngine.applyEvent(
            "SCALE_REFUSE_RECTIFY", null, null, now, 10, now);
        assertEquals(0, hitBottom.getScoreAfter());
        assertTrue(hitBottom.getAppliedDelta() > hitBottom.getRawDelta());

        ScoreChange hitTop = CreditScoringEngine.applyEvent(
            "PERIOD_CLEAN_90", null, null, now, 118, now);
        assertEquals(120, hitTop.getScoreAfter());
        assertTrue(hitTop.getAppliedDelta() < hitTop.getRawDelta());
    }

    @Test
    void timeDecayReducesPenaltyOverMonths() {
        LocalDateTime occurred = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);

        assertEquals(1.0, TimeDecay.decayFactor(occurred, occurred, 12));
        assertEquals(0.0, TimeDecay.decayFactor(occurred, now, 6));
        assertEquals(0.0, TimeDecay.decayFactor(occurred, now, 12));

        LocalDateTime sixMonthsLater = occurred.plusMonths(6);
        assertEquals(0.5, TimeDecay.decayFactor(occurred, sixMonthsLater, 12), 0.001);

        ScoringRule rule = ScoringRules.get("RECHECK_SHORTAGE");
        int raw = -10;
        int decayedFull = TimeDecay.applyDecay(raw, occurred, occurred, 12, true);
        int decayedHalf = TimeDecay.applyDecay(raw, occurred, sixMonthsLater, 12, true);
        int decayedZero = TimeDecay.applyDecay(raw, occurred, now, 12, true);

        assertEquals(-10, decayedFull);
        assertEquals(-5, decayedHalf);
        assertEquals(0, decayedZero);
    }

    @Test
    void bonusNeverDecays() {
        LocalDateTime old = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        int bonus = 5;
        int result = TimeDecay.applyDecay(bonus, old, now, 12, false);
        assertEquals(bonus, result);
    }

    @Test
    void patrolFrequencyScalesWithLevel() {
        assertEquals(2, CreditScoringEngine.recommendPatrolFrequency(CreditLevel.EXCELLENT, 4));
        assertEquals(3, CreditScoringEngine.recommendPatrolFrequency(CreditLevel.GOOD, 4));
        assertEquals(4, CreditScoringEngine.recommendPatrolFrequency(CreditLevel.AVERAGE, 4));
        assertEquals(6, CreditScoringEngine.recommendPatrolFrequency(CreditLevel.POOR, 4));
        assertEquals(10, CreditScoringEngine.recommendPatrolFrequency(CreditLevel.SERIOUS, 4));
    }

    @Test
    void redBlackListEligibility() {
        assertTrue(CreditScoringEngine.eligibleForRedList(115, 45));
        assertFalse(CreditScoringEngine.eligibleForRedList(100, 60));
        assertFalse(CreditScoringEngine.eligibleForRedList(115, 10));

        assertTrue(CreditScoringEngine.eligibleForBlackList(30));
        assertTrue(CreditScoringEngine.eligibleForBlackList(0));
        assertFalse(CreditScoringEngine.eligibleForBlackList(50));
    }

    @Test
    void recalculateFromHistoryWithDecay() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 0, 0);

        List<ScoreChange> history = new ArrayList<>();
        ScoringRule shortage = ScoringRules.get("RECHECK_SHORTAGE");
        history.add(new ScoreChange(shortage, "normal", 15, base, 100, base));
        history.add(new ScoreChange(ScoringRules.get("INSPECTION_PASS"), null, null,
            base.plusDays(10), 100, base.plusDays(10)));

        int recalculated = CreditScoringEngine.recalculateFromHistory(100, history, now);
        assertTrue(recalculated > 85);
        assertTrue(recalculated <= 100);
    }
}
