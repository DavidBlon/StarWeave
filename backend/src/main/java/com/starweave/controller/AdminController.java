package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.entity.Message;
import com.starweave.entity.User;
import com.starweave.entity.Wish;
import com.starweave.service.MessageService;
import com.starweave.service.UserService;
import com.starweave.service.WishService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员审核接口
 *
 * 鉴权方式：简单验证 userId 是否为管理员
 * 后续可升级为 JWT
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MessageService messageService;
    private final UserService userService;
    private final WishService wishService;

    public AdminController(MessageService messageService, UserService userService, WishService wishService) {
        this.messageService = messageService;
        this.userService = userService;
        this.wishService = wishService;
    }

    /**
     * 验证用户是否为管理员
     */
    private boolean isAdmin(Long userId) {
        if (userId == null) return false;
        User user = userService.findById(userId);
        return user != null && Boolean.TRUE.equals(user.getIsAdmin());
    }

    private ApiResponse<?> checkAdmin(Long userId) {
        if (!isAdmin(userId)) {
            return ApiResponse.error(403, "无权访问，需要管理员权限");
        }
        return null;
    }

    // ========== 流星审核 ==========

    /**
     * 获取待审核列表
     */
    @GetMapping("/pending")
    public ApiResponse<List<Message>> getPending(@RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<List<Message>>) err;
        return ApiResponse.success(messageService.findPending());
    }

    /**
     * 审核消息
     */
    @PostMapping("/review/{messageId}")
    public ApiResponse<Message> review(@PathVariable Long messageId,
                                       @RequestParam Long adminId,
                                       @RequestBody Map<String, String> body) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<Message>) err;

        String status = body.get("status");
        String reason = body.get("reason");

        if (!List.of("approved", "rejected").contains(status)) {
            return ApiResponse.badRequest("状态值无效，应为 approved 或 rejected");
        }

        Message message = messageService.review(messageId, status, reason);
        return ApiResponse.success(status.equals("approved") ? "已通过" : "已拒绝", message);
    }

    /**
     * 获取全部流星（可选按状态筛选）
     */
    @GetMapping("/messages")
    public ApiResponse<List<Message>> getMessages(@RequestParam Long adminId,
                                                   @RequestParam(required = false) String status) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<List<Message>>) err;
        return ApiResponse.success(messageService.findAllMessages(status));
    }

    /**
     * 删除流星（管理员）
     */
    @DeleteMapping("/meteors/{messageId}")
    public ApiResponse<?> deleteMeteor(@PathVariable Long messageId,
                                        @RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return err;
        messageService.deleteMeteor(messageId, null);
        return ApiResponse.success("流星已删除", null);
    }

    /**
     * 获取审核统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(@RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<Map<String, Object>>) err;

        Map<String, Long> stats = messageService.getStats();
        List<Message> pending = messageService.findPending();
        return ApiResponse.success(Map.of(
                "pendingCount", pending.size(),
                "approvedCount", stats.getOrDefault("approved", 0L),
                "rejectedCount", stats.getOrDefault("rejected", 0L),
                "totalCount", stats.getOrDefault("total", 0L)
        ));
    }

    // ========== 回复审核 ==========

    /**
     * 获取待审核回复
     */
    @GetMapping("/wishes/pending")
    public ApiResponse<List<Wish>> getPendingWishes(@RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<List<Wish>>) err;
        return ApiResponse.success(wishService.findPendingWishes());
    }

    /**
     * 获取全部回复（可选按状态筛选）
     */
    @GetMapping("/wishes")
    public ApiResponse<List<Wish>> getAllWishes(@RequestParam Long adminId,
                                                @RequestParam(required = false) String status) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<List<Wish>>) err;
        return ApiResponse.success(wishService.findAllWishes(status));
    }

    /**
     * 审核回复
     */
    @PostMapping("/wishes/{wishId}/review")
    public ApiResponse<Wish> reviewWish(@PathVariable Long wishId,
                                        @RequestParam Long adminId,
                                        @RequestBody Map<String, String> body) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<Wish>) err;

        String status = body.get("status");
        String reason = body.get("reason");

        if (!List.of("approved", "rejected").contains(status)) {
            return ApiResponse.badRequest("状态值无效，应为 approved 或 rejected");
        }

        Wish wish = wishService.reviewWish(wishId, status, reason);
        return ApiResponse.success(status.equals("approved") ? "已通过" : "已拒绝", wish);
    }

    /**
     * 删除回复（管理员）
     */
    @DeleteMapping("/wishes/{wishId}")
    public ApiResponse<?> deleteWish(@PathVariable Long wishId,
                                      @RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return err;
        wishService.deleteWish(wishId);
        return ApiResponse.success("回复已删除", null);
    }

    /**
     * 获取回复统计
     */
    @GetMapping("/wishes/stats")
    public ApiResponse<Map<String, Long>> getWishStats(@RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<Map<String, Long>>) err;
        return ApiResponse.success(wishService.getWishStats());
    }

    // ========== 用户管理 ==========

    /**
     * 获取全部用户
     */
    @GetMapping("/users")
    public ApiResponse<List<User>> getUsers(@RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return (ApiResponse<List<User>>) err;
        return ApiResponse.success(userService.findAll());
    }

    /**
     * 删除用户及其所有数据
     */
    @DeleteMapping("/users/{userId}")
    public ApiResponse<?> deleteUser(@PathVariable Long userId,
                                      @RequestParam Long adminId) {
        ApiResponse<?> err = checkAdmin(adminId);
        if (err != null) return err;
        userService.deleteUser(userId);
        return ApiResponse.success("用户已删除", null);
    }
}
