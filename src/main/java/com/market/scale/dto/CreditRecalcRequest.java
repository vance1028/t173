package com.market.scale.dto;

import jakarta.validation.constraints.NotNull;

public class CreditRecalcRequest {

    @NotNull(message = "摊位不能为空")
    private Long stallId;

    private String newRuleCode;

    private Integer newBaseDelta;

    private Integer newDecayMonths;

    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public String getNewRuleCode() { return newRuleCode; }
    public void setNewRuleCode(String newRuleCode) { this.newRuleCode = newRuleCode; }

    public Integer getNewBaseDelta() { return newBaseDelta; }
    public void setNewBaseDelta(Integer newBaseDelta) { this.newBaseDelta = newBaseDelta; }

    public Integer getNewDecayMonths() { return newDecayMonths; }
    public void setNewDecayMonths(Integer newDecayMonths) { this.newDecayMonths = newDecayMonths; }
}
