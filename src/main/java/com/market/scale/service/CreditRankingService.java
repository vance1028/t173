package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.credit.CreditLevel;
import com.market.scale.credit.CreditScoringEngine;
import com.market.scale.dto.RankingGenerateRequest;
import com.market.scale.entity.CreditRanking;
import com.market.scale.entity.CreditScore;
import com.market.scale.entity.Stall;
import com.market.scale.mapper.CreditRankingMapper;
import com.market.scale.mapper.CreditScoreMapper;
import com.market.scale.mapper.StallMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CreditRankingService {

    private final CreditRankingMapper rankingMapper;
    private final CreditScoreMapper scoreMapper;
    private final StallMapper stallMapper;
    private final CreditScoreService creditScoreService;

    public CreditRankingService(CreditRankingMapper rankingMapper, CreditScoreMapper scoreMapper,
                                StallMapper stallMapper, CreditScoreService creditScoreService) {
        this.rankingMapper = rankingMapper;
        this.scoreMapper = scoreMapper;
        this.stallMapper = stallMapper;
        this.creditScoreService = creditScoreService;
    }

    public Map<String, Object> page(String periodLabel, String rankingType, String publishStatus,
                                    int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<CreditRanking> rows = rankingMapper.search(periodLabel, rankingType, publishStatus,
            (p - 1) * s, s);
        long total = rankingMapper.count(periodLabel, rankingType, publishStatus);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    public List<String> listPublishedPeriods() {
        return rankingMapper.findPublishedPeriods();
    }

    @Transactional
    public Map<String, Object> generate(RankingGenerateRequest req) {
        String periodLabel = req.getPeriodLabel();
        String periodType = req.getPeriodType() == null ? "monthly" : req.getPeriodType();
        int redSize = req.getRedListSize() == null ? 10 : req.getRedListSize();
        int blackSize = req.getBlackListSize() == null ? 5 : req.getBlackListSize();

        if (periodLabel == null || periodLabel.isEmpty()) {
            periodLabel = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<CreditRanking> existingPublished = rankingMapper.search(periodLabel, null, "published", 0, 1000);
        if (!existingPublished.isEmpty()) {
            throw ApiException.conflict("该周期榜单已发布，不可重新生成草稿: " + periodLabel);
        }

        rankingMapper.deleteDraft(periodLabel, "red");
        rankingMapper.deleteDraft(periodLabel, "black");

        List<CreditScore> allScores = scoreMapper.findAllOrderByScore();
        List<CreditRanking> redList = new ArrayList<>();
        List<CreditRanking> blackList = new ArrayList<>();

        for (CreditScore cs : allScores) {
            Stall stall = stallMapper.findById(cs.getStallId());
            if (stall == null) continue;

            int cleanDays = creditScoreService.computeCleanDays(cs.getStallId(), LocalDateTime.now());
            boolean redEligible = CreditScoringEngine.eligibleForRedList(cs.getCurrentScore(), cleanDays);
            boolean blackEligible = CreditScoringEngine.eligibleForBlackList(cs.getCurrentScore());

            if (redEligible && redList.size() < redSize) {
                redList.add(buildRanking(periodType, periodLabel, "red", stall, cs, cleanDays));
            }
            if (blackEligible && blackList.size() < blackSize) {
                blackList.add(buildRanking(periodType, periodLabel, "black", stall, cs, cleanDays));
            }
        }

        redList.sort(Comparator.comparing(CreditRanking::getScore).reversed());
        blackList.sort(Comparator.comparing(CreditRanking::getScore));

        for (CreditRanking r : redList) rankingMapper.insert(r);
        for (CreditRanking r : blackList) rankingMapper.insert(r);

        Map<String, Object> res = new HashMap<>();
        res.put("periodLabel", periodLabel);
        res.put("periodType", periodType);
        res.put("redListCount", redList.size());
        res.put("blackListCount", blackList.size());
        res.put("redList", redList);
        res.put("blackList", blackList);
        return res;
    }

    private CreditRanking buildRanking(String periodType, String periodLabel, String rankingType,
                                       Stall stall, CreditScore cs, int cleanDays) {
        CreditRanking r = new CreditRanking();
        r.setPeriodType(periodType);
        r.setPeriodLabel(periodLabel);
        r.setRankingType(rankingType);
        r.setStallId(stall.getId());
        r.setStallNo(stall.getStallNo());
        r.setMerchantName(stall.getMerchantName());
        r.setMarketName(stall.getMarketName());
        r.setScore(cs.getCurrentScore());
        r.setLevelCode(cs.getLevelCode());
        r.setSnapshotData(String.format(
            "{\"category\":\"%s\",\"cleanDays\":%d,\"contactPhone\":\"%s\"}",
            stall.getCategory() == null ? "" : stall.getCategory(),
            cleanDays,
            stall.getContactPhone() == null ? "" : stall.getContactPhone()
        ));
        r.setPublishStatus("draft");
        return r;
    }

    @Transactional
    public List<CreditRanking> publish(String periodLabel, String rankingType) {
        List<CreditRanking> drafts = rankingMapper.search(periodLabel, rankingType, "draft", 0, 1000);
        if (drafts.isEmpty()) {
            throw ApiException.notFound("未找到该周期的草稿榜单");
        }
        LocalDateTime now = LocalDateTime.now();
        for (CreditRanking r : drafts) {
            r.setPublishStatus("published");
            r.setPublishedAt(now);
            rankingMapper.updatePublishStatus(r);
        }
        return drafts;
    }

    public Map<String, List<CreditRanking>> getPublishedByPeriod(String periodLabel) {
        List<CreditRanking> all = rankingMapper.search(periodLabel, null, "published", 0, 1000);
        Map<String, List<CreditRanking>> grouped = all.stream()
            .collect(Collectors.groupingBy(CreditRanking::getRankingType));
        grouped.computeIfAbsent("red", k -> new ArrayList<>());
        grouped.computeIfAbsent("black", k -> new ArrayList<>());
        return grouped;
    }
}
