package com.dhyey.chatapp_withjpa.websocket;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class WebSocketQueueInitializer {

    private final WebSocketMessageQueue messageQueue;

    public WebSocketQueueInitializer(WebSocketMessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @PostConstruct
    public void startProcessing() {
        messageQueue.processQueue(); // runs in async thread
    }
}