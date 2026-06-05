package com.starweave.mapper;

import com.starweave.entity.AiReviewLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiReviewLogMapper {

    AiReviewLog findById(@Param("id") Long id);

    AiReviewLog findByMessageId(@Param("messageId") Long messageId);

    List<AiReviewLog> findRecent(@Param("limit") int limit);

    List<AiReviewLog> findByResult(@Param("result") String result);

    int insert(AiReviewLog log);

    long countByResult(@Param("result") String result);
}
