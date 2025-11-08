package com.dhyey.chatapp_withjpa.configs;

import com.dhyey.chatapp_withjpa.websocket.WsHandler;
import com.dhyey.chatapp_withjpa.websocket.WsHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WsConfig implements WebSocketConfigurer {

    private final WsHandler wsHandler;
    private final WsHandshakeInterceptor wsHandshakeInterceptor;

    @Autowired
    public WsConfig(WsHandler wsHandler, WsHandshakeInterceptor wsHandshakeInterceptor) {
        this.wsHandler = wsHandler;
        this.wsHandshakeInterceptor = wsHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler, "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(wsHandshakeInterceptor);
    }
}
