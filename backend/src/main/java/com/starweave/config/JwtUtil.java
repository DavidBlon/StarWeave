package com.starweave.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成、验证、解析 token
 *
 * <p>密钥通过 {@code jwt.secret} 配置，默认使用 {@link #DEFAULT_SECRET}。
 * 生产环境务必通过环境变量 {@code JWT_SECRET} 设置一个足够长的随机字符串。</p>
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 默认密钥（仅用于开发环境，生产环境必须通过环境变量覆盖）
     */
    private static final String DEFAULT_SECRET =
            "starweave-default-jwt-secret-key-minimum-256-bits-long-change-in-production";

    private final SecretKey key;
    private final long expirationMs;

    @Autowired
    public JwtUtil(
            @Value("${jwt.secret:" + DEFAULT_SECRET + "}") String secret,
            @Value("${jwt.expiration:604800000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 为用户 ID 生成 JWT token，携带 tokenVersion 用于单设备登录校验
     */
    public String generateToken(Long userId, int tokenVersion) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tv", tokenVersion)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * 为用户 ID 生成 JWT token（默认版本 0，用于兼容场景）
     */
    public String generateToken(Long userId) {
        return generateToken(userId, 0);
    }

    /**
     * 从 token 中提取用户 ID，若 token 无效则返回 null
     */
    public Long getUserIdFromToken(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(subject);
        } catch (JwtException | NumberFormatException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 token 中提取 tokenVersion，若不存在或无效则返回 0
     */
    public int getTokenVersionFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("tv", Integer.class);
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * 验证 token 是否有效
     */
    public boolean validateToken(String token) {
        return getUserIdFromToken(token) != null;
    }
}
