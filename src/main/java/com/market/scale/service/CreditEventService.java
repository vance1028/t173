package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.credit.*;
import com.market.scale.dto.CreditEventRequest;
import com.market.scale.dto.RectifyRequest;
import com.market.scale.entity.CreditEvent;
import com.market.scale.entity.CreditFlow;
import com.market.scale.entity.CreditScore;
import com.market.scale.mapper.CreditEventMapper;
import com.market.scale.mapper.CreditFlowMapper;
import com.market.scale.mapper.CreditScoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreditEventService {

    private final CreditEventMapper eventMapper;
    private final CreditFlowMapper flowMapper;
    private final CreditScoreMapper scoreMapper;
    private final CreditScoreService creditScoreService;

    public CreditEventService(CreditEventMapper eventMapper, CreditFlowMapper flowMapper,
                              CreditScoreMapper scoreMapper, CreditScoreService creditScoreService) {
        this.eventMapper = eventMapper;
        this.flowMapper = flowMapper;
        this.scoreMapper = scoreMapper;
        this.creditScoreService = creditScoreService;
    }

    public Map<String, Object> page(Long stallId, String eventType, Boolean rectified, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<CreditEvent> rows = eventMapper.search(stallId, eventType, rectified, (p - 1) * s, s);
        long total = eventMapper.count(stallId, eventType, rectified);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    @Transactional
    public Map<String, Object> recordEvent(CreditEventRequest req) {
        if (!ScoringRules.exists(req.getEventType())) {
            throw ApiException.badRequest("未知的事件类型: " + req.getEventType());
        }

        CreditScore score = creditScoreService.getOrInit(req.getStallId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = now;

        CreditEvent event = new CreditEvent();
        event.setStallId(req.getStallId());
        event.setEventType(req.getEventType());
        event.setSeverity(req.getSeverity());
        event.setMagnitudeValue(req.getMagnitudeValue());
        event.setSourceTable(req.getSourceTable());
        event.setSourceId(req.getSourceId());
        event.setDescription(req.getDescription());
        event.setRectified(false);
        event.setOccurredAt(occurredAt);
        eventMapper.insert(event);

        ScoringRule rule = ScoringRules.get(req.getEventType());
        ScoreChange change = CreditScoringEngine.applyEvent(
            req.getEventType(), req.getSeverity(), req.getMagnitudeValue(),
            occurredAt, score.getCurrentScore(), now);

        CreditFlow flow = new CreditFlow();
        flow.setStallId(req.getStallId());
        flow.setEventId(event.getId());
        flow.setRuleCode(rule.getRuleCode());
        flow.setRuleName(rule.getRuleName());
        flow.setScoreBefore(change.getScoreBefore());
        flow.setScoreDeltaRaw(change.getRawDelta());
        flow.setScoreDeltaApplied(change.getAppliedDelta());
        flow.setScoreAfter(change.getScoreAfter());
        flow.setDecayMonths(rule.getDecayMonths());
        flow.setIsAddition(change.isAddition());
        flow.setRuleSnapshot(rule.toSnapshotJson());
        flow.setOccurredAt(occurredAt);
        flowMapper.insert(flow);

        score.setCurrentScore(change.getScoreAfter());
        score.setLevelCode(CreditLevel.fromScore(change.getScoreAfter()).getCode());

        if (!change.isAddition()) {
            score.setCleanDays(0);
            score.setLastEventAt(occurredAt);
        } else {
            int cleanDays = score.getLastEventAt() == null
                ? (int) ChronoUnit.DAYS.between(score.getCreatedAt(), now)
                : (int) ChronoUnit.DAYS.between(score.getLastEventAt(), now);
            score.setCleanDays(Math.max(score.getCleanDays(), cleanDays));
        }
        score.setLastRecalcAt(now);
        scoreMapper.update(score);

        Map<String, Object> result = new HashMap<>();
        result.put("event", event);
        result.put("flow", flow);
        result.put("score", score);
        return result;
    }

    @Transactional
    public Map<String, Object> rectify(RectifyRequest req) {
        CreditEvent event = eventMapper.findById(req.getEventId());
        if (event == null) {
            throw ApiException.notFound("事件不存在");
        }
        if (Boolean.TRUE.equals(event.getRectified())) {
            throw ApiException.conflict("该事件已整改");
        }

        LocalDateTime now = LocalDateTime.now();
        event.setRectified(true);
        event.setRectifiedAt(now);
        if (req.getRemark() != null && !req.getRemark().isEmpty()) {
            String desc = event.getDescription() == null ? "" : event.getDescription() + " | ";
            event.setDescription(desc + "整改说明: " + req.getRemark());
        }
        eventMapper.update(event);

        CreditScore score = creditScoreService.getOrInit(event.getStallId());
        ScoringRule rule = ScoringRules.get("ACTIVE_RECTIFY");
        ScoreChange change = CreditScoringEngine.applyEvent(
            "ACTIVE_RECTIFY", null, null, now, score.getCurrentScore(), now);

        CreditFlow flow = new CreditFlow();
        flow.setStallId(event.getStallId());
        flow.setEventId(event.getId());
        flow.setRuleCode(rule.getRuleCode());
        flow.setRuleName(rule.getRuleName());
        flow.setScoreBefore(change.getScoreBefore());
        flow.setScoreDeltaRaw(change.getRawDelta());
        flow.setScoreDeltaApplied(change.getAppliedDelta());
        flow.setScoreAfter(change.getScoreAfter());
        flow.setDecayMonths(rule.getDecayMonths());
        flow.setIsAddition(true);
        flow.setRuleSnapshot(rule.toSnapshotJson());
        flow.setOccurredAt(now);
        flowMapper.insert(flow);

        score.setCurrentScore(change.getScoreAfter());
        score.setLevelCode(CreditLevel.fromScore(change.getScoreAfter()).getCode());
        scoreMapper.update(score);

        Map<String, Object> result = new HashMap<>();
        result.put("event", event);
        result.put("flow", flow);
        result.put("score", score);
        return result;
    }

    public List<CreditEvent> findByStallSince(Long stallId, LocalDateTime since) {
        return eventMapper.findByStallSince(stallId, since);
    }

    @Transactional
    public void applyPeriodicBonus(Long stallId) {
        CreditScore score = creditScoreService.getOrInit(stallId);
        LocalDateTime now = LocalDateTime.now();
        int cleanDays = creditScoreService.computeCleanDays(stallId, now);

        if (cleanDays >= 90 && cleanDays % 90 < 1) {
            recordSimpleBonus(stallId, "PERIOD_CLEAN_90", now);
        } else if (cleanDays >= 30 && cleanDays % 30 < 1) {
            recordSimpleBonus(stallId, "PERIOD_CLEAN_30", now);
        }
    }

    private void recordSimpleBonus(Long stallId, String ruleCode, LocalDateTime now) {
        CreditScore score = creditScoreService.getOrInit(stallId);
        ScoringRule rule = ScoringRules.get(ruleCode);
        ScoreChange change = CreditScoringEngine.applyEvent(
            ruleCode, null, null, now, score.getCurrentScore(), now);

        CreditEvent event = new CreditEvent();
        event.setStallId(stallId);
        event.setEventType(ruleCode);
        event.setDescription("连续无违规自动加分");
        event.setRectified(true);
        event.setOccurredAt(now);
        eventMapper.insert(event);

        CreditFlow flow = new CreditFlow();
        flow.setStallId(stallId);
        flow.setEventId(event.getId());
        flow.setRuleCode(rule.getRuleCode());
        flow.setRuleName(rule.getRuleName());
        flow.setScoreBefore(change.getScoreBefore());
        flow.setScoreDeltaRaw(change.getRawDelta());
        flow.setScoreDeltaApplied(change.getAppliedDelta());
        flow.setScoreAfter(change.getScoreAfter());
        flow.setDecayMonths(rule.getDecayMonths());
        flow.setIsAddition(true);
        flow.setRuleSnapshot(rule.toSnapshotJson());
        flow.setOccurredAt(now);
        flowMapper.insert(flow);

        score.setCurrentScore(change.getScoreAfter());
        score.setLevelCode(CreditLevel.fromScore(change.getScoreAfter()).getCode());
        score.setCleanDays(creditScoreService.computeCleanDays(stallId, now));
        score.setLastRecalcAt(now);
        scoreMapper.update(score);
    }
}
