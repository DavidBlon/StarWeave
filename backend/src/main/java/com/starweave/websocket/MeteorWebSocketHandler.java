package com.starweave.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 处理器 — 实时推送新流星和捞起事件
 *
 * 前端连接 ws://host/ws/meteor 即可收到推送
 * 支持心跳检测（每 30 秒 ping，60 秒无响应断开）
 */
public class MeteorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MeteorWebSocketHandler.class);
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final long PING_INTERVAL_SECONDS = 30;
    private static final long MAX_IDLE_SECONDS = 60;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Long> lastActiveMap = new java.util.concurrent.ConcurrentHashMap<>();
    private ScheduledExecutorService heartbeatExecutor;

    @PostConstruct
    public void init() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatExecutor.scheduleAtFixedRate(this::heartbeatCheck,
                PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        lastActiveMap.put(session.getId(), System.currentTimeMillis());
        session.setTextMessageSizeLimit(64 * 1024);
        log.debug("WebSocket 连接建立: {} (当前连接数: {})", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        lastActiveMap.remove(session.getId());
        log.debug("WebSocket 连接关闭: {} (剩余: {})", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        // 处理客户端的 ping 心跳
        if ("{\"type\":\"ping\"}".equals(payload)) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                lastActiveMap.put(session.getId(), System.currentTimeMillis());
            } catch (IOException e) {
                log.warn("WebSocket 发送 pong 失败: {}", session.getId());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输错误: {} — {}", session.getId(), exception.getMessage());
        sessions.remove(session);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {}
    }

    /**
     * 心跳检测：发送 ping，清理过期连接
     */
    private void heartbeatCheck() {
        long now = System.currentTimeMillis();
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                lastActiveMap.remove(session.getId());
                continue;
            }
            Long lastActive = lastActiveMap.get(session.getId());
            long idleTime = now - (lastActive != null ? lastActive : now);
            if (idleTime > MAX_IDLE_SECONDS * 1000) {
                log.debug("WebSocket 连接 {} 超时（空闲 {}ms），关闭", session.getId(), idleTime);
                try {
                    session.close(CloseStatus.SESSION_NOT_RELIABLE);
                } catch (IOException ignored) {}
                sessions.remove(session);
                lastActiveMap.remove(session.getId());
                continue;
            }
            try {
                session.sendMessage(new PingMessage(ByteBuffer.wrap(new byte[0])));
                lastActiveMap.put(session.getId(), System.currentTimeMillis());
            } catch (IOException e) {
                log.warn("WebSocket ping 失败: {}", session.getId());
                sessions.remove(session);
                lastActiveMap.remove(session.getId());
            }
        }
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
        if (sessions.isEmpty()) return;
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
