package com.dhyey.chatapp_withjpa.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebSocketSessionRegistry {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        List<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(session);
            if (userSessions.isEmpty()) {
                sessions.remove(userId);
            }
        }
    }

    public List<WebSocketSession> getSessions(Long userId) {
        return sessions.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }

    public ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> getAllSessions() {
        return sessions;
    }
}