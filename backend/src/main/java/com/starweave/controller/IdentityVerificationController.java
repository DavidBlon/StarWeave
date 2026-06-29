package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.dto.IdentityVerifyRequest;
import com.starweave.dto.IdentityVerifyResult;
import com.starweave.service.IdentityVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/identity")
public class IdentityVerificationController {

    private final IdentityVerificationService identityVerificationService;

    public IdentityVerificationController(IdentityVerificationService identityVerificationService) {
        this.identityVerificationService = identityVerificationService;
    }

    @PostMapping("/id2/verify")
    public ApiResponse<IdentityVerifyResult> verifyId2(@Valid @RequestBody IdentityVerifyRequest request) {
        return ApiResponse.success(identityVerificationService.verifyId2(request));
    }
}
