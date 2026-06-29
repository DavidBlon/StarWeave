package com.starweave.service;

import com.starweave.dto.IdentityVerifyRequest;
import com.starweave.dto.IdentityVerifyResult;

public interface IdentityVerificationService {

    IdentityVerifyResult verifyId2(IdentityVerifyRequest request);
}
