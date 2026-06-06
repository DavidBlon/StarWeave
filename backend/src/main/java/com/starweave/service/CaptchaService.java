package com.starweave.service;

import com.starweave.dto.CaptchaResponse;

public interface CaptchaService {
    CaptchaResponse generate();

    boolean verify(String captchaId, String captchaCode);
}
