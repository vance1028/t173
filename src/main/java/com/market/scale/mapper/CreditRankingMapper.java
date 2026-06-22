package com.market.scale.mapper;

import com.market.scale.entity.CreditRanking;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreditRankingMapper {

    @Select("SELECT * FROM credit_rankings WHERE id = #{id}")
    CreditRanking findById(Long id);

    @Select("<script>" +
            "SELECT * FROM credit_rankings " +
            "<where>" +
            "  <if test='periodLabel != null and periodLabel != \"\"'>AND period_label = #{periodLabel}</if>" +
            "  <if test='rankingType != null and rankingType != \"\"'>AND ranking_type = #{rankingType}</if>" +
            "  <if test='publishStatus != null and publishStatus != \"\"'>AND publish_status = #{publishStatus}</if>" +
            "</where>" +
            " ORDER BY period_label DESC, score DESC, id LIMIT #{offset}, #{limit}" +
            "</script>")
    List<CreditRanking> search(@Param("periodLabel") String periodLabel,
                               @Param("rankingType") String rankingType,
                               @Param("publishStatus") String publishStatus,
                               @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM credit_rankings " +
            "<where>" +
            "  <if test='periodLabel != null and periodLabel != \"\"'>AND period_label = #{periodLabel}</if>" +
            "  <if test='rankingType != null and rankingType != \"\"'>AND ranking_type = #{rankingType}</if>" +
            "  <if test='publishStatus != null and publishStatus != \"\"'>AND publish_status = #{publishStatus}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("periodLabel") String periodLabel,
               @Param("rankingType") String rankingType,
               @Param("publishStatus") String publishStatus);

    @Select("SELECT DISTINCT period_label FROM credit_rankings WHERE publish_status = 'published' ORDER BY period_label DESC LIMIT 24")
    List<String> findPublishedPeriods();

    @Insert("INSERT INTO credit_rankings(period_type, period_label, ranking_type, stall_id, stall_no, merchant_name, market_name, score, level_code, snapshot_data, publish_status, published_at) " +
            "VALUES(#{periodType}, #{periodLabel}, #{rankingType}, #{stallId}, #{stallNo}, #{merchantName}, #{marketName}, #{score}, #{levelCode}, #{snapshotData}, #{publishStatus}, #{publishedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditRanking ranking);

    @Update("UPDATE credit_rankings SET publish_status=#{publishStatus}, published_at=#{publishedAt} WHERE id = #{id}")
    int updatePublishStatus(CreditRanking ranking);

    @Delete("DELETE FROM credit_rankings WHERE period_label = #{periodLabel} AND ranking_type = #{rankingType} AND publish_status = 'draft'")
    int deleteDraft(@Param("periodLabel") String periodLabel, @Param("rankingType") String rankingType);
}
