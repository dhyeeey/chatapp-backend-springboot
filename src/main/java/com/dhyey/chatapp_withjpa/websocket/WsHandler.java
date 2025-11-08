package com.dhyey.chatapp_withjpa.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WsHandler extends TextWebSocketHandler {

//    @Autowired
//    private WsMessageParser parser;

    @Autowired
    private WebSocketMessageQueue messageQueue;

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        Long userId = Long.parseLong(session.getAttributes().get("user_id").toString());
        sessionRegistry.addSession(userId, session);
        System.out.println("Connected: user=" + userId + " session=" + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        messageQueue.enqueue(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Long userId = Long.parseLong(session.getAttributes().get("user_id").toString());
        sessionRegistry.removeSession(userId, session);
        System.out.println("Disconnected: user=" + userId + " session=" + session.getId());
    }


}
