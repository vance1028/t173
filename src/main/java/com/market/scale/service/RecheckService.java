package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.dto.CreditEventRequest;
import com.market.scale.dto.RecheckRequest;
import com.market.scale.entity.RecheckRecord;
import com.market.scale.mapper.RecheckRecordMapper;
import com.market.scale.mapper.StallMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecheckService {

    private final RecheckRecordMapper recheckMapper;
    private final StallMapper stallMapper;
    private final CreditEventService creditEventService;

    public RecheckService(RecheckRecordMapper recheckMapper, StallMapper stallMapper,
                          @Lazy CreditEventService creditEventService) {
        this.recheckMapper = recheckMapper;
        this.stallMapper = stallMapper;
        this.creditEventService = creditEventService;
    }

    public Map<String, Object> page(Long stallId, String result, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<RecheckRecord> rows = recheckMapper.search(stallId, result, (p - 1) * s, s);
        long total = recheckMapper.count(stallId, result);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    public RecheckRecord create(RecheckRequest req) {
        if (stallMapper.findById(req.getStallId()) == null) {
            throw ApiException.badRequest("摊位不存在");
        }
        if (req.getActualWeightG() > req.getClaimedWeightG()) {
            throw ApiException.badRequest("实测重量大于计价重量，请核对录入");
        }
        int shortage = req.getClaimedWeightG() - req.getActualWeightG();
        RecheckRecord rec = new RecheckRecord();
        rec.setStallId(req.getStallId());
        rec.setCommodity(req.getCommodity());
        rec.setClaimedWeightG(req.getClaimedWeightG());
        rec.setActualWeightG(req.getActualWeightG());
        rec.setShortageG(shortage);
        rec.setResult(shortage > 0 ? "shortage" : "pass");
        rec.setHandledBy(req.getHandledBy());
        rec.setRemark(req.getRemark());
        rec.setRecheckedAt(LocalDateTime.now());
        recheckMapper.insert(rec);

        if (shortage > 0) {
            int shortagePercent = (int) Math.round(shortage * 100.0 / req.getClaimedWeightG());
            String severity;
            if (shortagePercent >= 20) severity = "serious";
            else if (shortagePercent >= 10) severity = "normal";
            else severity = "mild";

            CreditEventRequest eventReq = new CreditEventRequest();
            eventReq.setStallId(req.getStallId());
            eventReq.setEventType("RECHECK_SHORTAGE");
            eventReq.setSeverity(severity);
            eventReq.setMagnitudeValue(shortagePercent);
            eventReq.setSourceTable("recheck_records");
            eventReq.setSourceId(rec.getId());
            eventReq.setDescription(String.format("复称短缺: %s 短缺%d克(%.1f%%)",
                req.getCommodity(), shortage, shortage * 100.0 / req.getClaimedWeightG()));
            creditEventService.recordEvent(eventReq);
        } else {
            CreditEventRequest eventReq = new CreditEventRequest();
            eventReq.setStallId(req.getStallId());
            eventReq.setEventType("INSPECTION_PASS");
            eventReq.setSourceTable("recheck_records");
            eventReq.setSourceId(rec.getId());
            eventReq.setDescription(String.format("复称合格: %s", req.getCommodity()));
            creditEventService.recordEvent(eventReq);
        }

        return rec;
    }
}
