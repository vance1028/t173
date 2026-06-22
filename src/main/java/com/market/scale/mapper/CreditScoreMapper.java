package com.market.scale.mapper;

import com.market.scale.entity.CreditScore;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreditScoreMapper {

    @Select("SELECT * FROM credit_scores WHERE id = #{id}")
    CreditScore findById(Long id);

    @Select("SELECT * FROM credit_scores WHERE stall_id = #{stallId}")
    CreditScore findByStallId(Long stallId);

    @Select("<script>" +
            "SELECT * FROM credit_scores " +
            "<where>" +
            "  <if test='levelCode != null and levelCode != \"\"'>AND level_code = #{levelCode}</if>" +
            "</where>" +
            " ORDER BY current_score DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<CreditScore> search(@Param("levelCode") String levelCode,
                             @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM credit_scores " +
            "<where>" +
            "  <if test='levelCode != null and levelCode != \"\"'>AND level_code = #{levelCode}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("levelCode") String levelCode);

    @Insert("INSERT INTO credit_scores(stall_id, current_score, level_code, clean_days, last_event_at, last_recalc_at) " +
            "VALUES(#{stallId}, #{currentScore}, #{levelCode}, #{cleanDays}, #{lastEventAt}, #{lastRecalcAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditScore score);

    @Update("UPDATE credit_scores SET current_score=#{currentScore}, level_code=#{levelCode}, " +
            "clean_days=#{cleanDays}, last_event_at=#{lastEventAt}, last_recalc_at=#{lastRecalcAt} " +
            "WHERE id = #{id}")
    int update(CreditScore score);

    @Select("SELECT * FROM credit_scores ORDER BY current_score DESC")
    List<CreditScore> findAllOrderByScore();
}
