package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.entity.Sponsor;
import com.starweave.service.SponsorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsor")
public class SponsorController {

    private final SponsorService sponsorService;

    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    /**
     * 获取星光守护者列表（按金额降序）
     */
    @GetMapping("/guardians")
    public ApiResponse<List<Sponsor>> getGuardians() {
        return ApiResponse.success(sponsorService.findActive());
    }

    /**
     * 获取守护者数量
     */
    @GetMapping("/count")
    public ApiResponse<Long> count() {
        return ApiResponse.success(sponsorService.countActive());
    }
}
