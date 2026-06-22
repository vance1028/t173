package com.market.scale.mapper;

import com.market.scale.entity.CreditEvent;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CreditEventMapper {

    @Select("SELECT * FROM credit_events WHERE id = #{id}")
    CreditEvent findById(Long id);

    @Select("<script>" +
            "SELECT * FROM credit_events " +
            "<where>" +
            "  <if test='stallId != null'>AND stall_id = #{stallId}</if>" +
            "  <if test='eventType != null and eventType != \"\"'>AND event_type = #{eventType}</if>" +
            "  <if test='rectified != null'>AND rectified = #{rectified}</if>" +
            "</where>" +
            " ORDER BY occurred_at DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<CreditEvent> search(@Param("stallId") Long stallId,
                             @Param("eventType") String eventType,
                             @Param("rectified") Boolean rectified,
                             @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM credit_events " +
            "<where>" +
            "  <if test='stallId != null'>AND stall_id = #{stallId}</if>" +
            "  <if test='eventType != null and eventType != \"\"'>AND event_type = #{eventType}</if>" +
            "  <if test='rectified != null'>AND rectified = #{rectified}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("stallId") Long stallId,
               @Param("eventType") String eventType,
               @Param("rectified") Boolean rectified);

    @Select("SELECT * FROM credit_events WHERE stall_id = #{stallId} AND occurred_at >= #{since} ORDER BY occurred_at")
    List<CreditEvent> findByStallSince(Long stallId, LocalDateTime since);

    @Select("SELECT * FROM credit_events WHERE stall_id = #{stallId} ORDER BY occurred_at")
    List<CreditEvent> findByStallId(Long stallId);

    @Insert("INSERT INTO credit_events(stall_id, event_type, severity, magnitude_value, source_table, source_id, description, rectified, rectified_at, occurred_at) " +
            "VALUES(#{stallId}, #{eventType}, #{severity}, #{magnitudeValue}, #{sourceTable}, #{sourceId}, #{description}, #{rectified}, #{rectifiedAt}, #{occurredAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditEvent event);

    @Update("UPDATE credit_events SET rectified=#{rectified}, rectified_at=#{rectifiedAt}, description=#{description} WHERE id = #{id}")
    int update(CreditEvent event);
}
