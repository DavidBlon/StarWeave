package com.starweave.config;

import com.starweave.entity.User;
import com.starweave.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 从 {@code Authorization: Bearer <token>} 头中提取并验证 JWT，
 * 通过后将用户 ID 设置到 {@link SecurityContextHolder} 中。
 *
 * <p>此过滤器是可选（optional）的：没有 token 的请求仍可以匿名通过。
 * 单设备登录校验：验证 token 中的 {@code tokenVersion} 是否与数据库一致，
 * 不一致说明该账号已在其他地方登录，当前 token 失效。
 *
 * <p>公共路径（登录/注册/验证码）即使收到无效 token 也不会返回 401，
 * 确保用户在这些页面上不会因为残留的旧 token 被卡住无法操作。</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /** 不需要认证的公共路径——即使携带无效/过期 token 也不返回 401 */
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/captcha",
        "/api/user/register",
        "/api/user/login/password"
    );

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                // 校验 tokenVersion：确保单设备登录
                int tokenVersion = jwtUtil.getTokenVersionFromToken(token);
                User user = userMapper.findById(userId);
                int dbVersion = (user != null && user.getTokenVersion() != null) ? user.getTokenVersion() : 0;

                if (tokenVersion == dbVersion) {
                    // token 有效：设置认证信息
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authentication.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (!isPublicPath(request.getRequestURI())) {
                    // token 版本不匹配且不是公共路径 → 返回 401 踢下线
                    log.info("JWT 版本不匹配，拒绝认证: userId={}, tokenVer={}, dbVer={}",
                            userId, tokenVersion, dbVersion);
                    sendUnauthorized(response, "账号已在其他设备登录，请重新登录");
                    return;
                }
                // 公共路径 + 版本不匹配 → 匿名放行（让登录/注册/验证码正常使用）
            } else if (!isPublicPath(request.getRequestURI())) {
                // 无效 token（解析不出 userId）且不是公共路径 → 返回 401
                log.warn("收到无效 JWT: {}...", token.substring(0, Math.min(20, token.length())));
                sendUnauthorized(response, "登录已过期，请重新登录");
                return;
            }
            // 公共路径 + 无效 token → 匿名放行
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri) {
        return PUBLIC_PATHS.stream().anyMatch(uri::equals);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}"
        );
    }
}
