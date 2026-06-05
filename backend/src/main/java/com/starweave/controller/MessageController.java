package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.entity.Message;
import com.starweave.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 获取漂流中的流星
     */
    @GetMapping("/floating")
    public ApiResponse<List<Message>> getFloating(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(messageService.findFloating(limit));
    }

    /**
     * 获取消息详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Message> getById(@PathVariable Long id) {
        Message message = messageService.findById(id);
        if (message == null) {
            return ApiResponse.notFound("流星不存在");
        }
        return ApiResponse.success(message);
    }

    /**
     * 获取用户发布的流星
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Message>> getByUser(@PathVariable Long userId) {
        return ApiResponse.success(messageService.findByUserId(userId));
    }

    /**
     * 获取用户捞起的流星
     */
    @GetMapping("/caught/{userId}")
    public ApiResponse<List<Message>> getCaught(@PathVariable Long userId) {
        return ApiResponse.success(messageService.findByCatcher(userId));
    }

    /**
     * 发布流星
     */
    @PostMapping("/publish")
    public ApiResponse<Message> publish(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = (String) body.get("content");
        String color = (String) body.get("color");

        if (content == null || content.isBlank()) {
            return ApiResponse.badRequest("内容不能为空");
        }

        try {
            Message message = messageService.publish(userId, content, color);
            if ("rejected".equals(message.getStatus())) {
                return ApiResponse.success("流星未能通过审核，已消逝在夜空中", message);
            }
            return ApiResponse.success("流星已划入星海", message);
        } catch (Exception e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 捞起流星
     */
    @PostMapping("/catch")
    public ApiResponse<Message> catchMeteor(@RequestBody Map<String, Long> body) {
        Long messageId = body.get("messageId");
        Long userId = body.get("userId");

        try {
            boolean success = messageService.catchMeteor(messageId, userId);
            if (!success) {
                return ApiResponse.badRequest("流星已被他人捞走或不存在");
            }
            Message message = messageService.findById(messageId);
            return ApiResponse.success("你接住了一颗流星", message);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }
}
