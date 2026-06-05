package com.starweave.mapper;

import com.starweave.entity.StarMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StarMapMapper {

    StarMap findById(@Param("id") Long id);

    StarMap findByMessageId(@Param("messageId") Long messageId);

    List<StarMap> findByUserId(@Param("userId") Long userId);

    StarMap findByHash(@Param("contentHash") String contentHash);

    int insert(StarMap starMap);

    int unlockPremium(@Param("id") Long id, @Param("imageHdUrl") String imageHdUrl);

    int deleteById(@Param("id") Long id);

    int deleteByMessageId(@Param("messageId") Long messageId);

    int deleteByUserId(@Param("userId") Long userId);
}
