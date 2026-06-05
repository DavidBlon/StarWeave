package com.starweave.mapper;

import com.starweave.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    Message findById(@Param("id") Long id);

    /** 批量查询流星 */
    List<Message> findByIds(@Param("ids") List<Long> ids);

    /** 获取已审核通过、未被捞起的流星（按时间降序） */
    List<Message> findFloating(@Param("limit") int limit);

    /** 随机捞取一颗已审核的流星（排除 excludeUserId 的流星） */
    Message findRandomApproved(@Param("offset") int offset, @Param("excludeUserId") Long excludeUserId);

    /** 已审核通过且未被捞起的流星总数（排除 excludeUserId 的流星） */
    long countApproved(@Param("excludeUserId") Long excludeUserId);

    /** 获取用户发布的流星 */
    List<Message> findByUserId(@Param("userId") Long userId);

    /** 获取用户捞起的流星 */
    List<Message> findByCatcher(@Param("userId") Long userId);

    /** 待审核消息列表 */
    List<Message> findPending();

    int insert(Message message);

    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("reason") String reason,
                     @Param("healTag") String healTag,
                     @Param("healingMessage") String healingMessage);

    int catchMessage(@Param("id") Long id, @Param("userId") Long userId);

    int incrementWishCount(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    long count();

    long countByUserId(@Param("userId") Long userId);

    long countCaughtByUserId(@Param("userId") Long userId);

    /** 获取全部流星（管理员用），status 为 null 则全部 */
    List<Message> findAll(@Param("status") String status);

    /** 按状态统计 */
    long countByStatus(@Param("status") String status);

    /** 删除用户的所有流星 */
    int deleteByUserId(@Param("userId") Long userId);
}
