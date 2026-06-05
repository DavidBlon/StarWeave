package com.starweave.service.impl;

import com.starweave.entity.StarMap;
import com.starweave.mapper.StarMapMapper;
import com.starweave.service.StarMapService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class StarMapServiceImpl implements StarMapService {

    private final StarMapMapper starMapMapper;

    public StarMapServiceImpl(StarMapMapper starMapMapper) {
        this.starMapMapper = starMapMapper;
    }

    @Override
    public StarMap findById(Long id) {
        return starMapMapper.findById(id);
    }

    @Override
    public StarMap findByHash(String hash) {
        return starMapMapper.findByHash(hash);
    }

    @Override
    public List<StarMap> findByUserId(Long userId) {
        return starMapMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public StarMap generate(Long messageId, Long userId, String content) {
        // 确定性哈希：同一段话永远生成同一片星空
        String hash = sha256(content);
        StarMap existing = starMapMapper.findByHash(hash);
        if (existing != null) {
            return existing;
        }

        StarMap starMap = new StarMap();
        starMap.setMessageId(messageId);
        starMap.setUserId(userId);
        starMap.setContentHash(hash);

        // 生成星图数据的 URL（前端根据 hash 渲染，后端只存引用）
        starMap.setImageUrl("/api/star-map/render/" + hash + "?preview=true");
        starMap.setImageHdUrl("/api/star-map/render/" + hash + "?hd=true");
        starMap.setIsPremium(false);

        starMapMapper.insert(starMap);
        return starMap;
    }

    @Override
    @Transactional
    public StarMap unlockPremium(Long starMapId) {
        StarMap starMap = starMapMapper.findById(starMapId);
        if (starMap == null) {
            throw new RuntimeException("星图不存在");
        }
        starMapMapper.unlockPremium(starMapId, starMap.getImageHdUrl());
        starMap.setIsPremium(true);
        return starMap;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
