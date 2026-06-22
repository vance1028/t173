package com.market.scale.controller;

import com.market.scale.common.ApiResult;
import com.market.scale.dto.RankingGenerateRequest;
import com.market.scale.security.RequireRole;
import com.market.scale.service.CreditRankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credit/rankings")
@RequireRole
public class CreditRankingController {

    private final CreditRankingService rankingService;

    public CreditRankingController(CreditRankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public Map<String, Object> page(@RequestParam(required = false) String periodLabel,
                                    @RequestParam(required = false) String rankingType,
                                    @RequestParam(required = false) String publishStatus,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        return ApiResult.ok(rankingService.page(periodLabel, rankingType, publishStatus, page, size));
    }

    @GetMapping("/periods")
    public Map<String, Object> publishedPeriods() {
        List<String> periods = rankingService.listPublishedPeriods();
        return ApiResult.ok(periods);
    }

    @GetMapping("/period/{periodLabel}")
    public Map<String, Object> getByPeriod(@PathVariable String periodLabel) {
        return ApiResult.ok(rankingService.getPublishedByPeriod(periodLabel));
    }

    @PostMapping("/generate")
    @RequireRole({"admin"})
    public Map<String, Object> generate(@RequestBody(required = false) RankingGenerateRequest req) {
        if (req == null) req = new RankingGenerateRequest();
        return ApiResult.ok(rankingService.generate(req));
    }

    @PostMapping("/publish")
    @RequireRole({"admin"})
    public Map<String, Object> publish(@RequestParam String periodLabel,
                                       @RequestParam(defaultValue = "red,black") String rankingTypes) {
        String[] types = rankingTypes.split(",");
        for (String type : types) {
            rankingService.publish(periodLabel, type.trim());
        }
        return ApiResult.ok(rankingService.getPublishedByPeriod(periodLabel));
    }
}
