package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.entity.CatchHistory;
import com.starweave.entity.Message;
import com.starweave.entity.Wish;
import com.starweave.mapper.CatchHistoryMapper;
import com.starweave.mapper.WishMapper;
import com.starweave.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meteors")
public class MeteorController {

    private final MessageService messageService;
    private final WishMapper wishMapper;
    private final CatchHistoryMapper catchHistoryMapper;

    public MeteorController(MessageService messageService,
                            WishMapper wishMapper,
                            CatchHistoryMapper catchHistoryMapper) {
        this.messageService = messageService;
        this.wishMapper = wishMapper;
        this.catchHistoryMapper = catchHistoryMapper;
    }

    /**
     * 发布流星
     * POST /api/meteors
     */
    @PostMapping
    public ApiResponse<Message> publish(@RequestBody Map<String, String> body) {
        Long userId;
        try {
            userId = Long.parseLong(body.get("userId"));
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("无效的用户ID");
        }

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.badRequest("内容不能为空");
        }
        if (content.length() > 500) {
            return ApiResponse.badRequest("内容最长 500 个字符");
        }

        String color = body.get("color");
        Message message = messageService.publish(userId, content, color);
        return ApiResponse.success("发布成功，流星已飞入星海", message);
    }

    /**
     * 捞取随机流星
     * GET /api/meteors/random?userId=X
     */
    @GetMapping("/random")
    public ApiResponse<Message> catchRandom(@RequestParam(required = false) Long userId) {
        Message message = messageService.findRandomApproved(userId);
        if (message == null) {
            return ApiResponse.error(404, "星海暂无漂流中的流星");
        }
        return ApiResponse.success(message);
    }

    /**
     * 捞取流星（用户捞起）
     * POST /api/meteors/{id}/catch
     */
    @PostMapping("/{id}/catch")
    public ApiResponse<Message> catchMeteor(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        Long userId;
        try {
            userId = Long.parseLong(body.get("userId"));
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("无效的用户ID");
        }

        Message message = messageService.findById(id);
        if (message == null) {
            return ApiResponse.notFound("流星不存在");
        }
        if (message.getIsCaught()) {
            return ApiResponse.badRequest("这颗流星已经被捞走了");
        }
        if (message.getUserId().equals(userId)) {
            return ApiResponse.badRequest("不能捞起自己的流星");
        }

        boolean ok = messageService.catchMeteor(id, userId);
        if (!ok) {
            return ApiResponse.badRequest("流星已被他人捞走");
        }

        message.setIsCaught(true);
        message.setCaughtBy(userId);

        return ApiResponse.success("你捞起了一颗流星", message);
    }

    /**
     * 获取流星详情
     * GET /api/meteors/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<Message> getById(@PathVariable Long id) {
        Message message = messageService.findById(id);
        if (message == null) {
            return ApiResponse.notFound("流星不存在或已消逝");
        }
        return ApiResponse.success(message);
    }

    /**
     * 对流星许愿
     * POST /api/meteors/{id}/wish
     */
    @PostMapping("/{id}/wish")
    public ApiResponse<Void> makeWish(@PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        Long userId;
        try {
            userId = Long.parseLong(body.get("userId"));
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("无效的用户ID");
        }

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.badRequest("许愿内容不能为空");
        }

        Message message = messageService.findById(id);
        if (message == null) {
            return ApiResponse.notFound("流星不存在");
        }

        messageService.makeWish(id, userId, content);
        return ApiResponse.success("你的愿望已送达流星", null);
    }

    /**
     * 获取流星的许愿/回复列表（含回复者昵称）
     * GET /api/meteors/{id}/wishes
     */
    @GetMapping("/{id}/wishes")
    public ApiResponse<List<Wish>> getWishes(@PathVariable Long id) {
        List<Wish> wishes = wishMapper.findByMeteorIdWithUser(id);
        return ApiResponse.success(wishes);
    }

    /**
     * 获取用户发布的流星列表
     * GET /api/meteors/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Message>> getUserMeteors(@PathVariable Long userId) {
        List<Message> messages = messageService.findByUserId(userId);
        return ApiResponse.success(messages);
    }

    /**
     * 获取用户发布的流星 + 每条流星下的回复（含回复者昵称）
     * GET /api/meteors/user/{userId}/with-wishes
     */
    @GetMapping("/user/{userId}/with-wishes")
    public ApiResponse<List<Map<String, Object>>> getUserMeteorsWithWishes(@PathVariable Long userId) {
        List<Message> messages = messageService.findByUserId(userId);
        if (messages.isEmpty()) {
            return ApiResponse.success(List.of());
        }

        // 批量查询所有流星下的回复
        List<Long> meteorIds = messages.stream().map(Message::getId).collect(Collectors.toList());
        List<Wish> allWishes = wishMapper.findByMeteorIdsWithUser(meteorIds);

        // 按 meteorId 分组
        Map<Long, List<Wish>> wishesByMeteor = allWishes.stream()
                .collect(Collectors.groupingBy(Wish::getMeteorId));

        // 组装结果
        List<Map<String, Object>> result = messages.stream().map(msg -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", msg.getId());
            item.put("content", msg.getContent());
            item.put("color", msg.getColor());
            item.put("status", msg.getStatus());
            item.put("healTag", msg.getHealTag());
            item.put("healingMessage", msg.getHealingMessage());
            item.put("wishCount", msg.getWishCount());
            item.put("isCaught", msg.getIsCaught());
            item.put("caughtAt", msg.getCaughtAt());
            item.put("createdAt", msg.getCreatedAt());
            item.put("wishes", wishesByMeteor.getOrDefault(msg.getId(), List.of()));
            return item;
        }).collect(Collectors.toList());

        return ApiResponse.success(result);
    }

    /**
     * 获取用户发出的所有回复（含被回复的流星内容预览）
     * GET /api/wishes/user/{userId}
     */
    @GetMapping("/wishes/user/{userId}")
    public ApiResponse<List<Map<String, Object>>> getUserWishes(@PathVariable Long userId) {
        List<Wish> wishes = wishMapper.findByUserIdWithUser(userId);
        if (wishes.isEmpty()) {
            return ApiResponse.success(List.of());
        }

        // 批量查询被回复的流星内容
        List<Long> meteorIds = wishes.stream().map(Wish::getMeteorId).collect(Collectors.toList());
        List<Message> meteors = messageService.findByIds(meteorIds);
        Map<Long, String> meteorContent = meteors.stream()
                .collect(Collectors.toMap(Message::getId, Message::getContent));

        List<Map<String, Object>> result = wishes.stream().map(w -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", w.getId());
            item.put("meteorId", w.getMeteorId());
            item.put("content", w.getContent());
            item.put("replierNickname", w.getReplierNickname());
            item.put("createdAt", w.getCreatedAt());
            item.put("meteorContent", meteorContent.getOrDefault(w.getMeteorId(), ""));
            return item;
        }).collect(Collectors.toList());

        return ApiResponse.success(result);
    }

    /**
     * 删除自己的流星
     * DELETE /api/meteors/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMeteor(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        Long userId;
        try {
            userId = Long.parseLong(body.get("userId"));
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("无效的用户ID");
        }
        try {
            messageService.deleteMeteor(id, userId);
            return ApiResponse.success("流星已消逝在星河中", null);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 获取用户捞取的流星列表
     * GET /api/meteors/caught/{userId}
     */
    @GetMapping("/caught/{userId}")
    public ApiResponse<List<Message>> getCaughtMeteors(@PathVariable Long userId) {
        List<Message> messages = messageService.findByCatcher(userId);
        return ApiResponse.success(messages);
    }

    /**
     * 获取用户捞取历史
     * GET /api/meteors/caught/{userId}/history
     */
    @GetMapping("/caught/{userId}/history")
    public ApiResponse<List<CatchHistory>> getCatchHistory(@PathVariable Long userId) {
        List<CatchHistory> history = catchHistoryMapper.findByUserId(userId);
        return ApiResponse.success(history);
    }
}
