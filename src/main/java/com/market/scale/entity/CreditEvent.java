package com.market.scale.entity;

import java.time.LocalDateTime;

public class CreditEvent {
    private Long id;
    private Long stallId;
    private String eventType;
    private String severity;
    private Integer magnitudeValue;
    private String sourceTable;
    private Long sourceId;
    private String description;
    private Boolean rectified;
    private LocalDateTime rectifiedAt;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Boolean getRectified() { return rectified; }
    public void setRectified(Boolean rectified) { this.rectified = rectified; }

    public LocalDateTime getRectifiedAt() { return rectifiedAt; }
    public void setRectifiedAt(LocalDateTime rectifiedAt) { this.rectifiedAt = rectifiedAt; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
