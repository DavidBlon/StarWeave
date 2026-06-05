package com.starweave.controller;

import com.starweave.dto.ApiResponse;
import com.starweave.entity.StarMap;
import com.starweave.service.StarMapService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/star-map")
public class StarMapController {

    private final StarMapService starMapService;

    public StarMapController(StarMapService starMapService) {
        this.starMapService = starMapService;
    }

    @GetMapping("/{id}")
    public ApiResponse<StarMap> getById(@PathVariable Long id) {
        StarMap starMap = starMapService.findById(id);
        if (starMap == null) {
            return ApiResponse.notFound("星图不存在");
        }
        return ApiResponse.success(starMap);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<StarMap>> getByUser(@PathVariable Long userId) {
        return ApiResponse.success(starMapService.findByUserId(userId));
    }

    /**
     * 付费解锁高清星图
     */
    @PostMapping("/unlock")
    public ApiResponse<StarMap> unlock(@RequestBody Map<String, Long> body) {
        Long starMapId = body.get("starMapId");
        try {
            StarMap starMap = starMapService.unlockPremium(starMapId);
            return ApiResponse.success("星图已解锁", starMap);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    /**
     * 根据 hash 返回星图渲染数据（前端据此绘制 Canvas）
     */
    @GetMapping("/render/{hash}")
    public ApiResponse<StarMapRender> render(@PathVariable String hash,
                                             @RequestParam(defaultValue = "false") boolean hd) {
        StarMap starMap = starMapService.findByHash(hash);  // TODO: add findByHash to service
        if (starMap == null) {
            return ApiResponse.notFound("星图未生成");
        }

        // 高清版需要付费
        if (hd && !starMap.getIsPremium()) {
            return ApiResponse.forbidden("请先付费解锁高清星图");
        }

        return ApiResponse.success(new StarMapRender(starMap.getContentHash(), hd));
    }

    record StarMapRender(String hash, boolean hd) {}
}
