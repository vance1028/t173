package com.market.scale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreditEventRequest {

    @NotNull(message = "摊位不能为空")
    private Long stallId;

    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    private String severity;

    private Integer magnitudeValue;

    private String sourceTable;

    private Long sourceId;

    private String description;

    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Integer getMagnitudeValue() { return magnitudeValue; }
    public void setMagnitudeValue(Integer magnitudeValue) { this.magnitudeValue = magnitudeValue; }

    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
