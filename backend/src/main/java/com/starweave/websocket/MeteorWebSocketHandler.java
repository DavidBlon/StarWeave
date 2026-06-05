package com.starweave.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 处理器 — 实时推送新流星和捞起事件
 *
 * 前端连接 ws://host/ws/meteor 即可收到推送
 */
public class MeteorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MeteorWebSocketHandler.class);
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.debug("WebSocket 连接建立: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.debug("WebSocket 连接关闭: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端暂不需要发送消息，预留
    }

    /**
     * 广播新流星事件
     */
    public void broadcastNewMeteor(Long messageId, String content, String color) {
        broadcast(Map.of(
                "type", "new_meteor",
                "messageId", messageId,
                "content", content,
                "color", color
        ));
    }

    /**
     * 广播捞起事件
     */
    public void broadcastCaught(Long messageId, Long catcherId) {
        broadcast(Map.of(
                "type", "meteor_caught",
                "messageId", messageId,
                "catcherId", catcherId
        ));
    }

    private void broadcast(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("WebSocket 发送失败: {}", session.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("广播消息序列化失败", e);
        }
    }
}
