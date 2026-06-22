package com.market.scale.dto;

import jakarta.validation.constraints.NotNull;

public class RectifyRequest {

    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    private String remark;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
