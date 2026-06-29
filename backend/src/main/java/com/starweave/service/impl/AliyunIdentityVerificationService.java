package com.starweave.service.impl;

import com.aliyun.cloudauth20190307.Client;
import com.aliyun.cloudauth20190307.models.Id2MetaVerifyRequest;
import com.aliyun.cloudauth20190307.models.Id2MetaVerifyResponse;
import com.aliyun.cloudauth20190307.models.Id2MetaVerifyResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.starweave.config.AliyunCloudauthProperties;
import com.starweave.dto.IdentityVerifyRequest;
import com.starweave.dto.IdentityVerifyResult;
import com.starweave.service.IdentityVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AliyunIdentityVerificationService implements IdentityVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AliyunIdentityVerificationService.class);
    private static final String ALIYUN_SUCCESS_CODE = "200";
    private static final String MATCHED_BIZ_CODE = "1";

    private final AliyunCloudauthProperties properties;

    public AliyunIdentityVerificationService(AliyunCloudauthProperties properties) {
        this.properties = properties;
    }

    @Override
    public IdentityVerifyResult verifyId2(IdentityVerifyRequest request) {
        ensureConfigured();

        String paramType = normalizedParamType();
        EncodedIdentity encodedIdentity = encodeIdentity(request, paramType);

        Id2MetaVerifyRequest aliyunRequest = new Id2MetaVerifyRequest()
                .setParamType(paramType)
                .setUserName(encodedIdentity.userName())
                .setIdentifyNum(encodedIdentity.identifyNum());

        RuntimeOptions runtimeOptions = new RuntimeOptions()
                .setConnectTimeout(properties.getConnectTimeoutMs())
                .setReadTimeout(properties.getReadTimeoutMs());

        try {
            Id2MetaVerifyResponse response = createClient().id2MetaVerifyWithOptions(aliyunRequest, runtimeOptions);
            Id2MetaVerifyResponseBody body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Aliyun identity verification returned empty response");
            }

            String code = body.getCode();
            String requestId = body.getRequestId();
            if (!ALIYUN_SUCCESS_CODE.equals(code)) {
                log.warn("Aliyun Id2MetaVerify failed: requestId={}, code={}, message={}",
                        requestId, code, body.getMessage());
                throw new RuntimeException("Aliyun identity verification failed: " + code);
            }

            String bizCode = body.getResultObject() == null ? null : body.getResultObject().getBizCode();
            return new IdentityVerifyResult(MATCHED_BIZ_CODE.equals(bizCode), bizCode, requestId);
        } catch (Exception e) {
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Aliyun identity verification call failed", e);
        }
    }

    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setEndpoint(properties.getEndpoint());
        return new Client(config);
    }

    private void ensureConfigured() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Aliyun Cloudauth is disabled");
        }
        if (isBlank(properties.getAccessKeyId()) || isBlank(properties.getAccessKeySecret())) {
            throw new IllegalStateException("Aliyun Cloudauth credentials are missing");
        }
        if (isBlank(properties.getEndpoint())) {
            throw new IllegalStateException("Aliyun Cloudauth endpoint is missing");
        }
    }

    private String normalizedParamType() {
        String paramType = isBlank(properties.getParamType()) ? "normal" : properties.getParamType().strip();
        paramType = paramType.toLowerCase(Locale.ROOT);
        if (!"normal".equals(paramType) && !"md5".equals(paramType)) {
            throw new IllegalStateException("Unsupported Aliyun Cloudauth param-type: " + paramType);
        }
        return paramType;
    }

    private EncodedIdentity encodeIdentity(IdentityVerifyRequest request, String paramType) {
        String userName = request.getUserName().strip();
        String identifyNum = request.getIdentifyNum().strip();
        if ("normal".equals(paramType)) {
            return new EncodedIdentity(userName, identifyNum);
        }
        if (identifyNum.length() < 18) {
            throw new IllegalArgumentException("identifyNum is invalid for md5 param-type");
        }

        int firstCodePointEnd = userName.offsetByCodePoints(0, 1);
        String encodedUserName = md5Hex(userName.substring(0, firstCodePointEnd))
                + userName.substring(firstCodePointEnd);
        String encodedIdentifyNum = identifyNum.substring(0, 6)
                + md5Hex(identifyNum.substring(6, 14))
                + identifyNum.substring(14);
        return new EncodedIdentity(encodedUserName, encodedIdentifyNum);
    }

    private String md5Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is not available", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record EncodedIdentity(String userName, String identifyNum) {
    }
}
