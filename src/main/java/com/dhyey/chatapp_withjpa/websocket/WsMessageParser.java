package com.dhyey.chatapp_withjpa.websocket;

import com.dhyey.chatapp_withjpa.dto.AckMessageInserted;
import com.dhyey.chatapp_withjpa.dto.CreateConversationDTO;
import com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO;
import com.dhyey.chatapp_withjpa.dto.NewMessageDTOws;
import com.dhyey.chatapp_withjpa.entities.Conversation;
import com.dhyey.chatapp_withjpa.entities.Message;
import com.dhyey.chatapp_withjpa.entities.User;
import com.dhyey.chatapp_withjpa.services.ConversationService;
import com.dhyey.chatapp_withjpa.services.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WsMessageParser {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    // ---------------------------------------------------------------------
    // 🔹 Main entry point
    // ---------------------------------------------------------------------
    public void parse(TextMessage message, WebSocketSession session) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type.toLowerCase()) {
                case "new_message" -> handleNewMessage(json, session);
                case "ack_message_inserted" -> handleAckMessageInserted(json, session);
                case "create_new_conversation" -> handleCreateNewConversation(json, session);
                default -> System.err.println("⚠️ Unknown message type: " + type);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------
    // 🔹 Handle: new_message
    // ---------------------------------------------------------------------
    private void handleNewMessage(JsonNode json, WebSocketSession session) throws IOException {
        NewMessageDTOws newMessageDTOws = objectMapper.readValue(json.toString(), NewMessageDTOws.class);

        Conversation conversation = conversationService.getConversationByConversationId(
                newMessageDTOws.getConversationId());

        Message newMessage = messageService.insertMessageInConversation(newMessageDTOws);
        MessageForFrontendDTO messageForFrontendDTO = newMessage.getMessageForFrontendDTO();

        Map<String, Object> resp = new HashMap<>();
        resp.put("type", "new_message_inserted");
        resp.put("conversationId", conversation.getConversationId());
        resp.put("tempId", newMessageDTOws.getData().getMessage_id());
        resp.put("newId", newMessage.getId().getMessageId());
        resp.put("message", messageForFrontendDTO);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

    // ---------------------------------------------------------------------
    // 🔹 Handle: ack_message_inserted
    // ---------------------------------------------------------------------
    private void handleAckMessageInserted(JsonNode json, WebSocketSession session) throws IOException {
        AckMessageInserted ackMessageInserted = objectMapper.readValue(json.toString(), AckMessageInserted.class);
        Conversation conversation = conversationService.getConversationByConversationIdwithMembers(
                ackMessageInserted.getConversationId());

        Long senderUserId = Long.parseLong(session.getAttributes().get("user_id").toString());
        List<WebSocketSession> targetSessions = collectTargetSessions(conversation, session, senderUserId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("type", "new_message");
        resp.put("conversationId", ackMessageInserted.getConversationId());
        resp.put("message", ackMessageInserted.getMessage());

        String payload = objectMapper.writeValueAsString(resp);
        broadcastToSessions(targetSessions, payload);
    }

    // ---------------------------------------------------------------------
    // 🔹 Handle: create_new_conversation
    // ---------------------------------------------------------------------
    private void handleCreateNewConversation(JsonNode json, WebSocketSession session) throws IOException {
        CreateConversationDTO createConversationDTO = objectMapper.readValue(json.toString(), CreateConversationDTO.class);
        HashMap<String, Object> data = conversationService.createConversationWS(createConversationDTO);

        List<Long> broadcastUsers = List.of(
                createConversationDTO.getLoggeduserdata().getUser_id(),
                createConversationDTO.getOtheruserdata().getUserId()
        );

        for (Long userId : broadcastUsers) {
            List<WebSocketSession> sessions = sessionRegistry.getSessions(userId);
            if (sessions == null) continue;

            for (WebSocketSession s : sessions) {
                data.replace("makeconversationactive", s.getId().equals(session.getId()));
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // 🔹 Utility Methods
    // ---------------------------------------------------------------------
    private List<WebSocketSession> collectTargetSessions(Conversation conversation, WebSocketSession senderSession, Long senderUserId) {
        ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> allSessions = sessionRegistry.getAllSessions();
        List<WebSocketSession> targets = new ArrayList<>();

        // Sender’s other sessions
        List<WebSocketSession> senderSessions = allSessions.get(senderUserId);
        if (senderSessions != null) {
            senderSessions.stream()
                    .filter(s -> !s.getId().equals(senderSession.getId()))
                    .forEach(targets::add);
        }

        // Conversation members’ sessions
        for (User member : conversation.getMembers()) {
            Long memberUserId = member.getUserId();
            if (!memberUserId.equals(senderUserId) && allSessions.containsKey(memberUserId)) {
                targets.addAll(allSessions.get(memberUserId));
            }
        }

        return targets;
    }

    private void broadcastToSessions(List<WebSocketSession> sessions, String payload) {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcastToUsersExcludingCurrentSession(Collection<Long> userIds,
                                                        String payload,
                                                        WebSocketSession exclude) {
        for (Long userId : userIds) {
            List<WebSocketSession> sessions = sessionRegistry.getSessions(userId);
            if (sessions == null) continue;

            for (WebSocketSession s : sessions) {
                if (exclude != null && s.getId().equals(exclude.getId())) continue;
                if (s.isOpen()) {
                    try {
                        s.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
