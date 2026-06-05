package com.starweave.service;

import com.starweave.entity.Sponsor;

import java.util.List;

public interface SponsorService {

    List<Sponsor> findActive();

    /** 添加赞助记录（爱发电 webhook 回调时调用） */
    Sponsor addSponsor(String displayName, String message, java.math.BigDecimal amount, String platform);

    long countActive();
}
