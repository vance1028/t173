package com.market.scale.credit;

public enum Severity {

    MILD("mild", "轻微", 0.5),
    NORMAL("normal", "一般", 1.0),
    SERIOUS("serious", "严重", 2.0);

    private final String code;
    private final String label;
    private final double multiplier;

    Severity(String code, String label, double multiplier) {
        this.code = code;
        this.label = label;
        this.multiplier = multiplier;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public double getMultiplier() { return multiplier; }

    public static Severity fromCode(String code) {
        if (code == null) return NORMAL;
        for (Severity s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        return NORMAL;
    }
}
