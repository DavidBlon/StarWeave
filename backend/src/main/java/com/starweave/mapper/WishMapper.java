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

    /** 更新审核状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("reason") String reason);

    /** 待审核回复列表 */
    List<Wish> findPendingWishes();

    /** 全部回复（可选按状态筛选） */
    List<Wish> findAllWishes(@Param("status") String status);

    /** 按状态统计回复数 */
    long countWishByStatus(@Param("status") String status);

    /** 回复总数 */
    long countWish();

    /** 删除用户的所有回复 */
    int deleteByUserId(@Param("userId") Long userId);

    /** 管理员删除回复 */
    int deleteWishById(@Param("id") Long id);
}
