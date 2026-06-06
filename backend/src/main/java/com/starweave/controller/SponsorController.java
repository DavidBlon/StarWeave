package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.entity.Sponsor;
import com.starweave.service.SponsorService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sponsor")
public class SponsorController {

    private final SponsorService sponsorService;

    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    /**
     * 获取星光守护者列表（按金额降序）
     */
    @GetMapping("/guardians")
    public ApiResponse<List<Sponsor>> getGuardians() {
        return ApiResponse.success(sponsorService.findActive());
    }

    /**
     * 获取守护者数量
     */
    @GetMapping("/count")
    public ApiResponse<Long> count() {
        return ApiResponse.success(sponsorService.countActive());
    }

    @GetMapping("/afdian/oauth/start")
    public ApiResponse<Map<String, String>> startAfdianOAuth(@RequestParam Long userId) {
        try {
            return ApiResponse.success(Map.of("url", sponsorService.buildAfdianOAuthUrl(userId)));
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/afdian/oauth/callback")
    public ApiResponse<String> afdianOAuthCallback(@RequestParam String code, @RequestParam String state) {
        try {
            Long userId = parseUserIdFromRemark(state);
            if (userId == null) {
                return ApiResponse.badRequest("授权状态无效");
            }
            sponsorService.bindAfdianAccount(userId, code);
            return ApiResponse.success("爱发电账号绑定成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/mock-afdian")
    public ApiResponse<Sponsor> mockAfdian(@RequestBody Map<String, Object> body) {
        try {
            Long userId = getLong(body.get("userId"));
            BigDecimal amount = getAmount(body.get("amount"));
            String displayName = getString(body.get("displayName"), "爱发电赞助者");
            String message = getString(body.get("message"), "感谢支持 StarWeave");

            Sponsor sponsor = sponsorService.addSponsor(userId, displayName, message, amount, "afdian-mock");
            return ApiResponse.success("模拟赞助成功", sponsor);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/afdian/webhook")
    public Map<String, Object> afdianWebhook(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> order = getMap(getMap(body.get("data")).get("order"));
            Map<String, Object> payload = order.isEmpty() ? body : order;

            String afdianUserId = getString(payload.get("user_id"), "");
            String afdianUserPrivateId = getString(payload.get("user_private_id"), "");
            Long userId = sponsorService.findLinkedUserId(afdianUserId, afdianUserPrivateId);
            if (userId == null) {
                userId = getLong(payload.get("userId"));
            }
            if (userId == null) {
                userId = parseUserIdFromRemark(getString(payload.get("remark"), ""));
            }
            if (userId == null) {
                userId = parseUserIdFromRemark(getString(payload.get("message"), ""));
            }

            BigDecimal amount = getAmount(firstNonNull(payload.get("amount"), payload.get("total_amount"), payload.get("show_amount")));
            String displayName = getString(firstNonNull(payload.get("displayName"), payload.get("userName"), payload.get("user_id")), "爱发电赞助者");
            String message = getString(firstNonNull(payload.get("message"), payload.get("remark")), "感谢支持 StarWeave");

            Long status = getLong(payload.get("status"));
            if (userId != null && (status == null || status == 2)) {
                sponsorService.addSponsor(userId, displayName, message, amount, "afdian");
            }
            return Map.of("ec", 200, "em", "");
        } catch (Exception e) {
            return Map.of("ec", 200, "em", "");
        }
    }

    @GetMapping("/afdian/webhook")
    public Map<String, Object> afdianWebhookHealthCheck() {
        return Map.of("ec", 200, "em", "");
    }

    private Long getLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        String text = value.toString().strip();
        if (text.isEmpty()) return null;
        return Long.parseLong(text);
    }

    private BigDecimal getAmount(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal amount) return amount;
        return new BigDecimal(value.toString());
    }

    private String getString(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        String text = value.toString().strip();
        return text.isEmpty() ? defaultValue : text;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) return value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private Long parseUserIdFromRemark(String remark) {
        if (remark == null || remark.isBlank()) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("SW-(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(remark);
        if (!matcher.find()) return null;
        return Long.parseLong(matcher.group(1));
    }
}
