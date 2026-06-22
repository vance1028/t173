package com.market.scale.entity;

import java.time.LocalDateTime;

public class CreditScore {
    private Long id;
    private Long stallId;
    private Integer currentScore;
    private String levelCode;
    private Integer cleanDays;
    private LocalDateTime lastEventAt;
    private LocalDateTime lastRecalcAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public Integer getCurrentScore() { return currentScore; }
    public void setCurrentScore(Integer currentScore) { this.currentScore = currentScore; }

    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }

    public Integer getCleanDays() { return cleanDays; }
    public void setCleanDays(Integer cleanDays) { this.cleanDays = cleanDays; }

    public LocalDateTime getLastEventAt() { return lastEventAt; }
    public void setLastEventAt(LocalDateTime lastEventAt) { this.lastEventAt = lastEventAt; }

    public LocalDateTime getLastRecalcAt() { return lastRecalcAt; }
    public void setLastRecalcAt(LocalDateTime lastRecalcAt) { this.lastRecalcAt = lastRecalcAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
