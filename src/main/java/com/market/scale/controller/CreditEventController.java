package com.market.scale.controller;

import com.market.scale.common.ApiResult;
import com.market.scale.dto.CreditEventRequest;
import com.market.scale.dto.RectifyRequest;
import com.market.scale.security.RequireRole;
import com.market.scale.service.CreditEventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/credit/events")
@RequireRole
public class CreditEventController {

    private final CreditEventService creditEventService;

    public CreditEventController(CreditEventService creditEventService) {
        this.creditEventService = creditEventService;
    }

    @GetMapping
    public Map<String, Object> page(@RequestParam(required = false) Long stallId,
                                    @RequestParam(required = false) String eventType,
                                    @RequestParam(required = false) Boolean rectified,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(creditEventService.page(stallId, eventType, rectified, page, size));
    }

    @PostMapping
    @RequireRole({"admin", "inspector"})
    public Map<String, Object> create(@Valid @RequestBody CreditEventRequest req) {
        return ApiResult.ok(creditEventService.recordEvent(req));
    }

    @PostMapping("/rectify")
    @RequireRole({"admin", "inspector"})
    public Map<String, Object> rectify(@Valid @RequestBody RectifyRequest req) {
        return ApiResult.ok(creditEventService.rectify(req));
    }
}
