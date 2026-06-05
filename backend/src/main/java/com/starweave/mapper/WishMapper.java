package com.starweave.mapper;

import com.starweave.entity.Wish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishMapper {

    Wish findById(@Param("id") Long id);

    List<Wish> findByMeteorId(@Param("meteorId") Long meteorId);

    /** 查询某条流星的回复，含回复者昵称 */
    List<Wish> findByMeteorIdWithUser(@Param("meteorId") Long meteorId);

    /** 批量查询多条流星的回复，含回复者昵称 */
    List<Wish> findByMeteorIdsWithUser(@Param("meteorIds") List<Long> meteorIds);

    int insert(Wish wish);

    int deleteByMeteorId(@Param("meteorId") Long meteorId);

    long countByUserId(@Param("userId") Long userId);

    /** 查询某用户发出的所有回复，含回复者昵称 */
    List<Wish> findByUserIdWithUser(@Param("userId") Long userId);
}
