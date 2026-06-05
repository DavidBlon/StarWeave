package com.starweave.service;

import com.starweave.entity.StarMap;

import java.util.List;

public interface StarMapService {

    StarMap findById(Long id);

    /** 根据消息内容生成星图（确定性的伪随机，同一内容生成同一片星空） */
    StarMap generate(Long messageId, Long userId, String content);

    /** 根据 hash 查找星图 */
    StarMap findByHash(String hash);

    /** 获取用户的星图列表 */
    List<StarMap> findByUserId(Long userId);

    /** 付费解锁高清版 */
    StarMap unlockPremium(Long starMapId);
}
