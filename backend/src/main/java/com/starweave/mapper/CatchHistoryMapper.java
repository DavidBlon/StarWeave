package com.starweave.mapper;

import com.starweave.entity.CatchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CatchHistoryMapper {

    CatchHistory findById(@Param("id") Long id);

    List<CatchHistory> findByUserId(@Param("userId") Long userId);

    List<CatchHistory> findByMeteorId(@Param("meteorId") Long meteorId);

    int insert(CatchHistory catchHistory);

    int deleteByMeteorId(@Param("meteorId") Long meteorId);

    int deleteByUserId(@Param("userId") Long userId);
}
