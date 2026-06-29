package com.starweave.controller;

import com.starweave.config.JwtUtil;
import com.starweave.dto.ApiResponse;
import com.starweave.dto.LoginResult;
import com.starweave.dto.UserStats;
import com.starweave.entity.User;
import com.starweave.mapper.UserMapper;
import com.starweave.service.CaptchaService;
import com.starweave.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final String USERNAME_PATTERN = "^[A-Za-z0-9]+$";

    private final UserService userService;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserController(UserService userService, CaptchaService captchaService, JwtUtil jwtUtil, UserMapper userMapper) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    /**
     * 密码注册
     */
    @PostMapping("/register")
    public ApiResponse<LoginResult> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String nickname = body.get("nickname");
        String password = body.get("password");
        String captchaId = body.get("captchaId");
        String captcha = body.get("captcha");

        if (username == null || username.isBlank()) {
            return ApiResponse.badRequest("用户名不能为空");
        }
        if (nickname == null || nickname.isBlank()) {
            return ApiResponse.badRequest("名字不能为空");
        }
        if (password == null || password.length() < 6) {
            return ApiResponse.badRequest("密码至少 6 位");
        }
        if (!captchaService.verify(captchaId, captcha)) {
            return ApiResponse.badRequest("验证码错误或已过期");
        }
        username = username.strip();
        if (username.length() > 20) {
            return ApiResponse.badRequest("用户名最长 20 个字符");
        }
        nickname = nickname.strip();
        if (nickname.length() > 20) {
            return ApiResponse.badRequest("名字最长 20 个字符");
        }

        if (!isValidUsername(username)) {
            return ApiResponse.badRequest("用户名只能由英文和数字组成");
        }

        try {
            User user = userService.registerWithPassword(username, nickname, password);
            int tv = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
            String token = jwtUtil.generateToken(user.getId(), tv);
            return ApiResponse.success("注册成功", new LoginResult(user, token));
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 密码登录
     */
    @PostMapping("/login/password")
    public ApiResponse<LoginResult> loginWithPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String captchaId = body.get("captchaId");
        String captcha = body.get("captcha");

        if (username == null || username.isBlank()) {
            return ApiResponse.badRequest("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return ApiResponse.badRequest("密码不能为空");
        }

        if (!captchaService.verify(captchaId, captcha)) {
            return ApiResponse.badRequest("验证码错误或已过期");
        }

        try {
            User user = userService.loginWithPassword(username.strip(), password);
            int tv = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
            String token = jwtUtil.generateToken(user.getId(), tv);
            return ApiResponse.success("登录成功", new LoginResult(user, token));
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResponse.notFound("用户不存在");
        }
        return ApiResponse.success(user);
    }

    /**
     * 更新个人资料（昵称 / 签名）
     */
    @PutMapping("/{id}")
    public ApiResponse<User> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        String bio = body.containsKey("bio") ? body.get("bio") : null;
        try {
            User user = userService.updateProfile(id, nickname, bio);
            return ApiResponse.success("更新成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/{id}/avatar")
    public ApiResponse<User> uploadAvatar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String avatarUrl = body.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return ApiResponse.badRequest("头像数据不能为空");
        }
        try {
            User user = userService.updateAvatar(id, avatarUrl);
            return ApiResponse.success("头像已更新", user);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/{id}/password")
    public ApiResponse<User> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            return ApiResponse.badRequest("请输入旧密码");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.badRequest("新密码至少 6 位");
        }

        try {
            User user = userService.changePassword(id, oldPassword, newPassword);
            return ApiResponse.success("密码修改成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/{id}/stats")
    public ApiResponse<UserStats> getUserStats(@PathVariable Long id) {
        UserStats stats = userService.getUserStats(id);
        return ApiResponse.success(stats);
    }

    /**
     * 头像文件上传（multipart）
     */
    @PostMapping("/{id}/avatar/upload")
    public ApiResponse<User> uploadAvatarFile(@PathVariable Long id,
                                               @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.badRequest("请选择文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.badRequest("仅支持图片文件");
        }
        try {
            // 安全清理原始文件名：只保留最后一段，移除路径分隔符和潜在危险字符
            String originalName = file.getOriginalFilename();
            String safeName = (originalName != null)
                    ? originalName.substring(Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\')) + 1)
                         .replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "avatar";
            if (safeName.isBlank() || ".".equals(safeName) || "..".equals(safeName)) {
                safeName = "avatar.png";
            }
            String filename = "avatar_" + id + "_" + System.currentTimeMillis() + "_" + safeName;
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            java.io.File dest = new java.io.File(uploadDir, filename);
            file.transferTo(dest);
            // 只存文件名，前端通过 /api/user/{id}/avatar/raw 来获取
            User user = userService.updateAvatar(id, filename);
            return ApiResponse.success("头像上传成功", user);
        } catch (Exception e) {
            return ApiResponse.error(500, "头像上传失败");
        }
    }

    /**
     * 获取原始头像文件（避免 Windows file: URI 问题）
     */
    @GetMapping("/{id}/avatar/raw")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getRawAvatar(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null || user.getAvatarUrl() == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        // 兼容纯文件名 和 /uploads/avatars/filename 两种格式
        String avatarUrl = user.getAvatarUrl();
        String filename = avatarUrl;
        int lastSep = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSep >= 0) {
            filename = filename.substring(lastSep + 1);
        }

        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(
                System.getProperty("user.dir"), "uploads", "avatars", filename);
            java.io.File file = filePath.toFile();
            if (!file.exists()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            org.springframework.core.io.Resource resource =
                new org.springframework.core.io.FileSystemResource(file);

            String contentType = "application/octet-stream";
            try {
                contentType = java.nio.file.Files.probeContentType(filePath);
            } catch (Exception ignored) {}
            if (contentType == null) contentType = "application/octet-stream";

            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .cacheControl(org.springframework.http.CacheControl.maxAge(365, java.util.concurrent.TimeUnit.DAYS))
                    .body(resource);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 退出登录：递增 token_version，使当前 token 立即失效
     * 无需请求体，从 JWT 中提取用户 ID
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            return ApiResponse.badRequest("未登录");
        }
        userMapper.incrementTokenVersion(userId);
        return ApiResponse.success("已退出登录", null);
    }

    @GetMapping("/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.findAll());
    }

    private boolean isValidUsername(String username) {
        return username.matches(USERNAME_PATTERN);
    }
}
