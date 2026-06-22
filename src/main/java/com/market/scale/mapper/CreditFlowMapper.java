package com.market.scale.mapper;

import com.market.scale.entity.CreditFlow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreditFlowMapper {

    @Select("SELECT * FROM credit_flows WHERE id = #{id}")
    CreditFlow findById(Long id);

    @Select("SELECT * FROM credit_flows WHERE stall_id = #{stallId} ORDER BY occurred_at DESC, id DESC LIMIT #{offset}, #{limit}")
    List<CreditFlow> findByStallId(@Param("stallId") Long stallId,
                                   @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM credit_flows WHERE stall_id = #{stallId}")
    long countByStallId(Long stallId);

    @Select("SELECT * FROM credit_flows WHERE event_id = #{eventId} ORDER BY occurred_at")
    List<CreditFlow> findByEventId(Long eventId);

    @Select("SELECT * FROM credit_flows WHERE stall_id = #{stallId} ORDER BY occurred_at")
    List<CreditFlow> findAllByStallId(Long stallId);

    @Insert("INSERT INTO credit_flows(stall_id, event_id, rule_code, rule_name, score_before, score_delta_raw, score_delta_applied, score_after, decay_months, is_addition, rule_snapshot, occurred_at) " +
            "VALUES(#{stallId}, #{eventId}, #{ruleCode}, #{ruleName}, #{scoreBefore}, #{scoreDeltaRaw}, #{scoreDeltaApplied}, #{scoreAfter}, #{decayMonths}, #{isAddition}, #{ruleSnapshot}, #{occurredAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditFlow flow);

    @Delete("DELETE FROM credit_flows WHERE stall_id = #{stallId}")
    int deleteByStallId(Long stallId);
}
