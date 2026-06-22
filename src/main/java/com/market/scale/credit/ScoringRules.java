package com.market.scale.credit;

import java.util.HashMap;
import java.util.Map;

public final class ScoringRules {

    private static final Map<String, ScoringRule> REGISTRY = new HashMap<>();

    static {
        register(new ScoringRule("RECHECK_SHORTAGE", "复称短缺", -10,
            true, true, 12, MagnitudeMultiplier.shortagePercent()));
        register(new ScoringRule("SCALE_EXPIRED", "计量器具超期未检", -15,
            false, true, 6, MagnitudeMultiplier.expiredDays()));
        register(new ScoringRule("SCALE_UNQUALIFIED", "计量器具检定不合格", -20,
            true, false, 12, MagnitudeMultiplier.none()));
        register(new ScoringRule("SCALE_REFUSE_RECTIFY", "拒不整改", -30,
            false, false, 24, MagnitudeMultiplier.none()));
        register(new ScoringRule("PERIOD_CLEAN_30", "连续30天无违规", 3,
            false, false, 0, MagnitudeMultiplier.none()));
        register(new ScoringRule("PERIOD_CLEAN_90", "连续90天无违规", 8,
            false, false, 0, MagnitudeMultiplier.none()));
        register(new ScoringRule("ACTIVE_RECTIFY", "主动整改到位", 5,
            false, false, 0, MagnitudeMultiplier.none()));
        register(new ScoringRule("INSPECTION_PASS", "抽检合格", 2,
            false, false, 0, MagnitudeMultiplier.none()));
    }

    private ScoringRules() {}

    private static void register(ScoringRule rule) {
        REGISTRY.put(rule.getRuleCode(), rule);
    }

    public static ScoringRule get(String ruleCode) {
        return REGISTRY.get(ruleCode);
    }

    public static Map<String, ScoringRule> all() {
        return new HashMap<>(REGISTRY);
    }

    public static boolean exists(String ruleCode) {
        return REGISTRY.containsKey(ruleCode);
    }
}
