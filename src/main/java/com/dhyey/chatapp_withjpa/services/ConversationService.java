package com.dhyey.chatapp_withjpa.services;

import com.dhyey.chatapp_withjpa.dto.*;
import com.dhyey.chatapp_withjpa.entities.*;
import com.dhyey.chatapp_withjpa.repositories.ConversationMemberRepository;
import com.dhyey.chatapp_withjpa.repositories.ConversationRepository;
import com.dhyey.chatapp_withjpa.repositories.MessageRepository;
import com.dhyey.chatapp_withjpa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Transactional
    public HashMap<String,Object> createConversationWS(CreateConversationDTO body){
        LoggedUserData loggeduserdata = body.getLoggeduserdata();
        GlobalUsersSearch otheruserdata = body.getOtheruserdata();

        Optional<User> user1 = userRepository.findByUserId(loggeduserdata.getUser_id());
        Optional<User> user2 = userRepository.findByUserId(otheruserdata.getUserId());

        HashSet<User> users = new HashSet<>();
        users.add(user1.get());
        users.add(user2.get());

        Conversation conversation = Conversation.builder()
                .title(null).isGroup(false).createdAt(body.getMessage().getSent_at())
                .lastUpdated(body.getMessage().getSent_at())
                .members(users)
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        // ✅ Add members before fetching the DTO
        conversationMemberRepository.saveAll(
                users.stream()
                        .map(u -> new ConversationMember(u, savedConversation, Instant.now()))
                        .toList()
        );

        Long nextId = messageRepository.findMaxMessageId(conversation.getConversationId());
        MessageId id = new MessageId(conversation.getConversationId(), nextId);

        MessageForFrontendDTO messageForFrontendDTO = body.getMessage();

        Message message = Message.builder()
                .id(id)
                .conversation(savedConversation)
                .content(messageForFrontendDTO.getContent())
                .sender(user1.get())
                .sentAt(body.getMessage().getSent_at())
                .isDeleted(false)
                .build();

        MessageForFrontendDTO savedMessage = messageRepository.save(message).getMessageForFrontendDTO();

        ConversationFrontendDTO conversationFrontendDTO = getConversationBetweenTwoUsers(user1.get().getUserId(), user2.get().getUserId());

        return new HashMap<>(){{
            put("type","created_new_conversation");
            put("message",savedMessage);
            put("conversation",conversationFrontendDTO);
            put("makeconversationactive",false);
        }};

    }

    public ConversationFrontendDTO getConversationBetweenTwoUsers(Long user1, Long user2) {
        ConversationFrontendDTO conversation = conversationRepository.
                findDirectConversation(user1, user2).orElse(null);

        List<ConversationMemberDTO> conversationMemberDTO = conversationMemberRepository.
                findConversationMembersByConversationId(conversation.getConversationId());

        conversation.setMembers(conversationMemberDTO);
        conversation.setGroup_info(GroupInfoDTO.builder().is_group(conversation.getIsGroup()).group_avatar(null)
                .group_desc(null).group_name(conversation.getTitle()).build());
        conversation.setAllMessagesLoaded(false);
        conversation.setWebsocketOrInputCounter(0);
        conversation.setDataFetchingCounter(0);

        return conversation;
    }

    @Transactional(readOnly = true)
    public Conversation getConversationByConversationIdwithMembers(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        conversation.getMembers().size();
        return conversation;
    }

    public Conversation getConversationByConversationId(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }

    public Optional<Long> findDirectConversationIdByUserId(Long userId1, Long userId2) {
        return conversationRepository.findDirectConversationIdByUserId(userId1, userId2);
    }

    public List<ConversationFrontendDTO> getAllConversationsOfUser(Long userId) {
        List<Object[]> result = conversationRepository.findConversationsOfUser(userId);
        List<ConversationFrontendDTO> conversations = new ArrayList<>();

        Map<Long, List<Object[]>> groupedConvoMembers =
                result.stream()
                        .collect(Collectors.groupingBy(
                                o -> {
                                    return ((Number) o[0]).longValue();
                                },
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        for(Map.Entry<Long, List<Object[]>> entry : groupedConvoMembers.entrySet()) {

            List<Object[]> convoMembers = entry.getValue();

            ConversationFrontendDTO conversation = new ConversationFrontendDTO();
            conversation.setConversationId(((Number)convoMembers.get(0)[0]).longValue());
            conversation.setCreatedAt(convoMembers.get(0)[1] instanceof Instant ? (Instant) convoMembers.get(0)[1] : ((Timestamp) convoMembers.get(0)[1]).toInstant());
            conversation.setTitle((String) convoMembers.get(0)[2]);
            conversation.setIsGroup((Boolean) convoMembers.get(0)[3]);
            conversation.setLastUpdated((Instant) convoMembers.get(0)[9]);

            conversation.setMembers(convoMembers.stream().map(rw -> {
                ConversationMemberDTO conversationMemberDTO = new ConversationMemberDTO();
                conversationMemberDTO.setConversationId(((Number)rw[0]).longValue());
                conversationMemberDTO.setUserId(((Number)rw[4]).longValue());
                conversationMemberDTO.setUsername(((String)rw[5]));
                conversationMemberDTO.setProfileName(((String)rw[6]));
                conversationMemberDTO.setAvatar(((String)rw[7]));
                conversationMemberDTO.setJoinedAt(rw[8] instanceof Instant ? (Instant) rw[8] : ((Timestamp) rw[8]).toInstant());

                return conversationMemberDTO;
            }).toList());

            conversation.setGroup_info(GroupInfoDTO.builder()
                            .group_name(conversation.getTitle())
                            .is_group(conversation.getIsGroup())
                            .group_desc(null).group_avatar(null)
                    .build());

            conversations.add(conversation);
        }

        return conversations;
    }



}
