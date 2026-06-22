package com.market.scale.mapper;

import com.market.scale.entity.CreditRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreditRuleMapper {

    @Select("SELECT * FROM credit_rules WHERE id = #{id}")
    CreditRule findById(Long id);

    @Select("SELECT * FROM credit_rules WHERE rule_code = #{ruleCode}")
    CreditRule findByCode(String ruleCode);

    @Select("SELECT * FROM credit_rules ORDER BY id")
    List<CreditRule> findAll();

    @Select("SELECT * FROM credit_rules WHERE enabled = 1 ORDER BY id")
    List<CreditRule> findEnabled();

    @Insert("INSERT INTO credit_rules(rule_code, rule_name, score_delta, has_severity, has_magnitude, decay_months, description, enabled) " +
            "VALUES(#{ruleCode}, #{ruleName}, #{scoreDelta}, #{hasSeverity}, #{hasMagnitude}, #{decayMonths}, #{description}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditRule rule);

    @Update("UPDATE credit_rules SET rule_name=#{ruleName}, score_delta=#{scoreDelta}, has_severity=#{hasSeverity}, " +
            "has_magnitude=#{hasMagnitude}, decay_months=#{decayMonths}, description=#{description}, enabled=#{enabled} " +
            "WHERE id = #{id}")
    int update(CreditRule rule);
}
