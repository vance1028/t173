package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.credit.*;
import com.market.scale.entity.CreditFlow;
import com.market.scale.entity.CreditScore;
import com.market.scale.mapper.CreditFlowMapper;
import com.market.scale.mapper.CreditScoreMapper;
import com.market.scale.mapper.StallMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreditScoreService {

    private final CreditScoreMapper scoreMapper;
    private final CreditFlowMapper flowMapper;
    private final StallMapper stallMapper;

    public CreditScoreService(CreditScoreMapper scoreMapper, CreditFlowMapper flowMapper, StallMapper stallMapper) {
        this.scoreMapper = scoreMapper;
        this.flowMapper = flowMapper;
        this.stallMapper = stallMapper;
    }

    @Transactional
    public CreditScore getOrInit(Long stallId) {
        if (stallMapper.findById(stallId) == null) {
            throw ApiException.notFound("摊位不存在");
        }
        CreditScore score = scoreMapper.findByStallId(stallId);
        if (score == null) {
            score = new CreditScore();
            score.setStallId(stallId);
            score.setCurrentScore(ScoreConstants.BASE_SCORE);
            score.setLevelCode(CreditLevel.GOOD.getCode());
            score.setCleanDays(0);
            scoreMapper.insert(score);
        }
        return score;
    }

    public CreditScore findByStallId(Long stallId) {
        return scoreMapper.findByStallId(stallId);
    }

    public Map<String, Object> page(String levelCode, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<CreditScore> rows = scoreMapper.search(levelCode, (p - 1) * s, s);
        long total = scoreMapper.count(levelCode);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    public Map<String, Object> flowPage(Long stallId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        getOrInit(stallId);
        List<CreditFlow> rows = flowMapper.findByStallId(stallId, (p - 1) * s, s);
        long total = flowMapper.countByStallId(stallId);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    public List<CreditFlow> getAllFlows(Long stallId) {
        return flowMapper.findAllByStallId(stallId);
    }

    public int computeCleanDays(Long stallId, LocalDateTime now) {
        CreditScore score = scoreMapper.findByStallId(stallId);
        if (score == null || score.getLastEventAt() == null) {
            return (int) ChronoUnit.DAYS.between(
                stallMapper.findById(stallId).getCreatedAt().toLocalDate().atStartOfDay(), now);
        }
        return (int) ChronoUnit.DAYS.between(score.getLastEventAt(), now);
    }

    public int getPatrolFrequency(Long stallId, int baseFrequency) {
        CreditScore score = getOrInit(stallId);
        CreditLevel level = CreditLevel.fromScore(score.getCurrentScore());
        return CreditScoringEngine.recommendPatrolFrequency(level, baseFrequency);
    }

    @Transactional
    public int recalculate(Long stallId) {
        CreditScore score = getOrInit(stallId);
        LocalDateTime now = LocalDateTime.now();
        List<CreditFlow> flows = flowMapper.findAllByStallId(stallId);
        List<ScoreChange> changes = new ArrayList<>();
        for (CreditFlow f : flows) {
            ScoringRule rule = ScoringRules.get(f.getRuleCode());
            if (rule == null) continue;
            ScoreChange change = new ScoreChange(rule, null, null, f.getOccurredAt(),
                ScoreConstants.BASE_SCORE, now);
            changes.add(change);
        }
        int newScore = CreditScoringEngine.recalculateFromHistory(ScoreConstants.BASE_SCORE, changes, now);
        score.setCurrentScore(newScore);
        score.setLevelCode(CreditLevel.fromScore(newScore).getCode());
        score.setLastRecalcAt(now);
        scoreMapper.update(score);
        return newScore;
    }

    public Map<String, Object> getLevelInfo() {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> levels = new ArrayList<>();
        for (CreditLevel level : CreditLevel.values()) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", level.getCode());
            m.put("label", level.getLabel());
            m.put("minScore", level.getMinScore() == Integer.MIN_VALUE ? 0 : level.getMinScore());
            m.put("maxScore", level.getMaxScore() == Integer.MAX_VALUE ? ScoreConstants.MAX_SCORE : level.getMaxScore());
            m.put("patrolMultiplier", level.getPatrolMultiplier());
            levels.add(m);
        }
        res.put("levels", levels);
        res.put("baseScore", ScoreConstants.BASE_SCORE);
        res.put("minScore", ScoreConstants.MIN_SCORE);
        res.put("maxScore", ScoreConstants.MAX_SCORE);
        return res;
    }
}
