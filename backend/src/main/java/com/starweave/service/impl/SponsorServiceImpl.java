package com.starweave.service.impl;

import com.starweave.entity.Sponsor;
import com.starweave.mapper.SponsorMapper;
import com.starweave.service.SponsorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SponsorServiceImpl implements SponsorService {

    private final SponsorMapper sponsorMapper;

    public SponsorServiceImpl(SponsorMapper sponsorMapper) {
        this.sponsorMapper = sponsorMapper;
    }

    @Override
    public List<Sponsor> findActive() {
        return sponsorMapper.findActive();
    }

    @Override
    @Transactional
    public Sponsor addSponsor(String displayName, String message, BigDecimal amount, String platform) {
        Sponsor sponsor = new Sponsor();
        sponsor.setDisplayName(displayName);
        sponsor.setMessage(message);
        sponsor.setAmount(amount);
        sponsor.setPlatform(platform);
        sponsor.setBorderStyle("sponsor");
        sponsor.setIsActive(true);

        sponsorMapper.insert(sponsor);
        return sponsor;
    }

    @Override
    public long countActive() {
        return sponsorMapper.countActive();
    }
}
