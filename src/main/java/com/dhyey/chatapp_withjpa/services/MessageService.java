package com.dhyey.chatapp_withjpa.services;

import com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO;
import com.dhyey.chatapp_withjpa.dto.NewMessageDTOws;
import com.dhyey.chatapp_withjpa.entities.Conversation;
import com.dhyey.chatapp_withjpa.entities.Message;
import com.dhyey.chatapp_withjpa.entities.MessageId;
import com.dhyey.chatapp_withjpa.entities.User;
import com.dhyey.chatapp_withjpa.exceptions.UnauthorizedMessageException;
import com.dhyey.chatapp_withjpa.repositories.ConversationRepository;
import com.dhyey.chatapp_withjpa.repositories.MessageRepository;
import com.dhyey.chatapp_withjpa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Message insertMessageInConversation(NewMessageDTOws newMessageDTOws) {

        // Step 1: Fetch conversation and sender
        Conversation conversation = conversationRepository.findByConversationId(newMessageDTOws.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        User sender = userRepository.findByUserId(newMessageDTOws.getData().getSender_user_id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Step 2: Validate that sender is part of conversation
        boolean isMember = conversation.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(sender.getUserId()));

        if (!isMember) {
            throw new UnauthorizedMessageException("Sender is not part of this conversation");
        }

        // Step 3: Get next message_id (max + 1)
        Long maxMessageId = messageRepository.findMaxMessageId(conversation.getConversationId());
        long nextId = (maxMessageId == null ? 1 : maxMessageId + 1);

        // Step 4: Build and save message
        Message message = Message.builder()
                .id(new MessageId(conversation.getConversationId(), nextId))
                .conversation(conversation)
                .sender(sender)
                .content(newMessageDTOws.getData().getContent())
                .isDeleted(false)
                .sentAt(newMessageDTOws.getData().getSent_at() != null ? newMessageDTOws.getData().getSent_at() : Instant.now())
                .build();

        // ✅ Save the message
        Message savedMessage = messageRepository.save(message);

        // ✅ Update conversation lastUpdated explicitly
        conversation.setLastUpdated(savedMessage.getSentAt());
        conversationRepository.save(conversation);  // flush the change

        return savedMessage;
    }

    public List<MessageForFrontendDTO> findLastestInitialMessagesNativeQuery(List<Long> conversationIds, Integer limit) {
        return messageRepository.findLastestInitialMessagesNativeQuery(conversationIds, limit);
    }

    public List<MessageForFrontendDTO> getPrevMessages(Long conversationId, Long userId, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findPreviousMessagesDesc(conversationId,userId, pageable);
    }

    public List<MessageForFrontendDTO> getPrevMessagesTesting(Long conversationId, Long userId, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findPreviousMessagesDesc(conversationId,userId, pageable).reversed();
    }
}
