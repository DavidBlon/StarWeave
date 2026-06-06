package com.starweave.mapper;

import com.starweave.entity.AfdianAccountLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AfdianAccountLinkMapper {
    AfdianAccountLink findByUserId(@Param("userId") Long userId);

    AfdianAccountLink findByAfdianUserId(@Param("afdianUserId") String afdianUserId);

    AfdianAccountLink findByAfdianUserPrivateId(@Param("afdianUserPrivateId") String afdianUserPrivateId);

    int upsert(AfdianAccountLink link);
}
