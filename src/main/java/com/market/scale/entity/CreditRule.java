package com.market.scale.entity;

import java.time.LocalDateTime;

public class CreditRule {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private Integer scoreDelta;
    private Boolean hasSeverity;
    private Boolean hasMagnitude;
    private Integer decayMonths;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public Integer getScoreDelta() { return scoreDelta; }
    public void setScoreDelta(Integer scoreDelta) { this.scoreDelta = scoreDelta; }

    public Boolean getHasSeverity() { return hasSeverity; }
    public void setHasSeverity(Boolean hasSeverity) { this.hasSeverity = hasSeverity; }

    public Boolean getHasMagnitude() { return hasMagnitude; }
    public void setHasMagnitude(Boolean hasMagnitude) { this.hasMagnitude = hasMagnitude; }

    public Integer getDecayMonths() { return decayMonths; }
    public void setDecayMonths(Integer decayMonths) { this.decayMonths = decayMonths; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
