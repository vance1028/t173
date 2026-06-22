package com.market.scale.dto;

public class RankingGenerateRequest {

    private String periodLabel;

    private String periodType;

    private Integer redListSize;

    private Integer blackListSize;

    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public Integer getRedListSize() { return redListSize; }
    public void setRedListSize(Integer redListSize) { this.redListSize = redListSize; }

    public Integer getBlackListSize() { return blackListSize; }
    public void setBlackListSize(Integer blackListSize) { this.blackListSize = blackListSize; }
}
