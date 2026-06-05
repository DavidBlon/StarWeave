package com.starweave.mapper;

import com.starweave.entity.Sponsor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SponsorMapper {

    Sponsor findById(@Param("id") Long id);

    /** 获取活跃的守护者列表 */
    List<Sponsor> findActive();

    List<Sponsor> findByUserId(@Param("userId") Long userId);

    int insert(Sponsor sponsor);

    int deactivate(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    long countActive();
}
