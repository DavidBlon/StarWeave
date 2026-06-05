package com.starweave.config;

import com.starweave.websocket.MeteorWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meteorWebSocketHandler(), "/ws/meteor")
                .setAllowedOrigins("*");
    }

    @Bean
    public MeteorWebSocketHandler meteorWebSocketHandler() {
        return new MeteorWebSocketHandler();
    }
}
