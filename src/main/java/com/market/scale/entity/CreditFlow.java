package com.market.scale.entity;

import java.time.LocalDateTime;

public class CreditFlow {
    private Long id;
    private Long stallId;
    private Long eventId;
    private String ruleCode;
    private String ruleName;
    private Integer scoreBefore;
    private Integer scoreDeltaRaw;
    private Integer scoreDeltaApplied;
    private Integer scoreAfter;
    private Integer decayMonths;
    private Boolean isAddition;
    private String ruleSnapshot;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public Integer getScoreBefore() { return scoreBefore; }
    public void setScoreBefore(Integer scoreBefore) { this.scoreBefore = scoreBefore; }

    public Integer getScoreDeltaRaw() { return scoreDeltaRaw; }
    public void setScoreDeltaRaw(Integer scoreDeltaRaw) { this.scoreDeltaRaw = scoreDeltaRaw; }

    public Integer getScoreDeltaApplied() { return scoreDeltaApplied; }
    public void setScoreDeltaApplied(Integer scoreDeltaApplied) { this.scoreDeltaApplied = scoreDeltaApplied; }

    public Integer getScoreAfter() { return scoreAfter; }
    public void setScoreAfter(Integer scoreAfter) { this.scoreAfter = scoreAfter; }

    public Integer getDecayMonths() { return decayMonths; }
    public void setDecayMonths(Integer decayMonths) { this.decayMonths = decayMonths; }

    public Boolean getIsAddition() { return isAddition; }
    public void setIsAddition(Boolean isAddition) { this.isAddition = isAddition; }

    public String getRuleSnapshot() { return ruleSnapshot; }
    public void setRuleSnapshot(String ruleSnapshot) { this.ruleSnapshot = ruleSnapshot; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
