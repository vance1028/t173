package com.market.scale.controller;

import com.market.scale.common.ApiResult;
import com.market.scale.credit.CreditLevel;
import com.market.scale.entity.CreditScore;
import com.market.scale.security.RequireRole;
import com.market.scale.service.CreditScoreService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/credit/scores")
@RequireRole
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    public CreditScoreController(CreditScoreService creditScoreService) {
        this.creditScoreService = creditScoreService;
    }

    @GetMapping
    public Map<String, Object> page(@RequestParam(required = false) String levelCode,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(creditScoreService.page(levelCode, page, size));
    }

    @GetMapping("/stall/{stallId}")
    public Map<String, Object> getByStall(@PathVariable Long stallId) {
        CreditScore score = creditScoreService.getOrInit(stallId);
        Map<String, Object> res = new HashMap<>();
        res.put("score", score);
        CreditLevel level = CreditLevel.fromScore(score.getCurrentScore());
        Map<String, Object> levelInfo = new HashMap<>();
        levelInfo.put("code", level.getCode());
        levelInfo.put("label", level.getLabel());
        levelInfo.put("patrolMultiplier", level.getPatrolMultiplier());
        res.put("level", levelInfo);
        res.put("cleanDays", creditScoreService.computeCleanDays(stallId, java.time.LocalDateTime.now()));
        res.put("recommendedPatrolFrequency", creditScoreService.getPatrolFrequency(stallId, 4));
        return ApiResult.ok(res);
    }

    @GetMapping("/stall/{stallId}/flows")
    public Map<String, Object> flows(@PathVariable Long stallId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(creditScoreService.flowPage(stallId, page, size));
    }

    @PostMapping("/stall/{stallId}/recalc")
    @RequireRole({"admin"})
    public Map<String, Object> recalculate(@PathVariable Long stallId) {
        int newScore = creditScoreService.recalculate(stallId);
        Map<String, Object> res = new HashMap<>();
        res.put("stallId", stallId);
        res.put("recalculatedScore", newScore);
        res.put("level", CreditLevel.fromScore(newScore).getLabel());
        return ApiResult.ok(res);
    }

    @GetMapping("/levels")
    public Map<String, Object> levels() {
        return ApiResult.ok(creditScoreService.getLevelInfo());
    }
}
