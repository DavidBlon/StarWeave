package com.starweave.service.impl;

import com.starweave.entity.Sponsor;
import com.starweave.entity.AfdianAccountLink;
import com.starweave.mapper.AfdianAccountLinkMapper;
import com.starweave.mapper.SponsorMapper;
import com.starweave.mapper.UserMapper;
import com.starweave.service.SponsorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class SponsorServiceImpl implements SponsorService {

    private final SponsorMapper sponsorMapper;
    private final UserMapper userMapper;
    private final AfdianAccountLinkMapper afdianAccountLinkMapper;
    private final RestTemplate restTemplate;

    @Value("${afdian.oauth.client-id:}")
    private String afdianClientId;

    @Value("${afdian.oauth.client-secret:}")
    private String afdianClientSecret;

    @Value("${afdian.oauth.redirect-uri:}")
    private String afdianRedirectUri;

    public SponsorServiceImpl(SponsorMapper sponsorMapper, UserMapper userMapper,
                              AfdianAccountLinkMapper afdianAccountLinkMapper, RestTemplate restTemplate) {
        this.sponsorMapper = sponsorMapper;
        this.userMapper = userMapper;
        this.afdianAccountLinkMapper = afdianAccountLinkMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Sponsor> findActive() {
        return sponsorMapper.findActive();
    }

    @Override
    @Transactional
    public Sponsor addSponsor(String displayName, String message, BigDecimal amount, String platform) {
        return addSponsor(null, displayName, message, amount, platform);
    }

    @Override
    @Transactional
    public Sponsor addSponsor(Long userId, String displayName, String message, BigDecimal amount, String platform) {
        if (userId != null && userMapper.findById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }

        Sponsor sponsor = new Sponsor();
        sponsor.setUserId(userId);
        sponsor.setDisplayName(displayName);
        sponsor.setMessage(message);
        sponsor.setAmount(amount);
        sponsor.setPlatform(platform);
        sponsor.setBorderStyle("sponsor");
        sponsor.setIsActive(true);

        sponsorMapper.insert(sponsor);

        if (userId != null) {
            userMapper.setSponsor(userId, true);
            userMapper.updateBorderStyle(userId, "sponsor");
        }

        return sponsor;
    }

    @Override
    public long countActive() {
        return sponsorMapper.countActive();
    }

    @Override
    public String buildAfdianOAuthUrl(Long userId) {
        if (userId == null || userMapper.findById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }
        if (afdianClientId == null || afdianClientId.isBlank() || afdianRedirectUri == null || afdianRedirectUri.isBlank()) {
            throw new RuntimeException("爱发电 OAuth 未配置");
        }
        return UriComponentsBuilder.fromUriString("https://afdian.net/oauth2/authorize")
                .queryParam("client_id", afdianClientId)
                .queryParam("redirect_uri", afdianRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "basic")
                .queryParam("state", "SW-" + userId)
                .build()
                .toUriString();
    }

    @Override
    @Transactional
    public void bindAfdianAccount(Long userId, String code) {
        if (userId == null || userMapper.findById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }
        if (code == null || code.isBlank()) {
            throw new RuntimeException("授权码不能为空");
        }
        if (afdianClientId == null || afdianClientId.isBlank() || afdianClientSecret == null || afdianClientSecret.isBlank()) {
            throw new RuntimeException("爱发电 OAuth 未配置");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", afdianClientId);
        form.add("client_secret", afdianClientSecret);
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", afdianRedirectUri);

        Map<?, ?> response = restTemplate.postForObject(
                "https://afdian.net/api/oauth2/access_token",
                new HttpEntity<>(form, headers),
                Map.class
        );
        Map<?, ?> data = getMap(response == null ? null : response.get("data"));
        String afdianUserId = getString(data.get("user_id"));
        String afdianUserPrivateId = getString(data.get("user_private_id"));
        if (afdianUserId == null) {
            throw new RuntimeException("爱发电授权失败");
        }

        AfdianAccountLink link = new AfdianAccountLink();
        link.setUserId(userId);
        link.setAfdianUserId(afdianUserId);
        link.setAfdianUserPrivateId(afdianUserPrivateId);
        afdianAccountLinkMapper.upsert(link);
    }

    @Override
    public Long findLinkedUserId(String afdianUserId, String afdianUserPrivateId) {
        AfdianAccountLink link = null;
        if (afdianUserPrivateId != null && !afdianUserPrivateId.isBlank()) {
            link = afdianAccountLinkMapper.findByAfdianUserPrivateId(afdianUserPrivateId);
        }
        if (link == null && afdianUserId != null && !afdianUserId.isBlank()) {
            link = afdianAccountLinkMapper.findByAfdianUserId(afdianUserId);
        }
        return link == null ? null : link.getUserId();
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> getMap(Object value) {
        if (value instanceof Map<?, ?> map) return map;
        return Map.of();
    }

    private String getString(Object value) {
        if (value == null) return null;
        String text = value.toString().strip();
        return text.isEmpty() ? null : text;
    }
}
