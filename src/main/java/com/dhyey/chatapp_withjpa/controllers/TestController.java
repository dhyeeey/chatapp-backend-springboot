package com.dhyey.chatapp_withjpa.controllers;

import com.dhyey.chatapp_withjpa.dto.ConversationFrontendDTO;
import com.dhyey.chatapp_withjpa.dto.FetchPrevMessageDTO;
import com.dhyey.chatapp_withjpa.dto.LoggedUserData;
import com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO;
import com.dhyey.chatapp_withjpa.entities.Conversation;
import com.dhyey.chatapp_withjpa.entities.Message;
import com.dhyey.chatapp_withjpa.entities.MessageId;
import com.dhyey.chatapp_withjpa.entities.User;
import com.dhyey.chatapp_withjpa.repositories.ConversationMemberRepository;
import com.dhyey.chatapp_withjpa.repositories.ConversationRepository;
import com.dhyey.chatapp_withjpa.repositories.MessageRepository;
import com.dhyey.chatapp_withjpa.repositories.UserRepository;
import com.dhyey.chatapp_withjpa.services.ConversationService;
import com.dhyey.chatapp_withjpa.services.MessageService;
import com.dhyey.chatapp_withjpa.services.UserService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test/api")
public class TestController {

    MessageRepository messageRepository;
    ConversationRepository conversationRepository;
    UserRepository userRepository;
    ConversationMemberRepository conversationMemberRepository;
    ConversationService conversationService;
    MessageService messageService;
    UserService userService;

    @Autowired
    public TestController(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          UserRepository userRepository,
                          ConversationMemberRepository conversationMemberRepository,
                          ConversationService conversationService,
                          MessageService messageService,
                          UserService userService
    ) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/loggeduserdata/{id}")
    ResponseEntity<?> getLoggeduserdata(@PathVariable("id") Long id) {

        Optional<LoggedUserData> loggedUserDataProjection = userRepository.findUserByLoggedUserId(id);

        return ResponseEntity.ok(loggedUserDataProjection);
    }

    @GetMapping("/globalusers")
    ResponseEntity<?> getGlobalUsers() {
        return ResponseEntity.ok(userRepository.findByUsernameContainingIgnoreCaseOrProfileNameContainingIgnoreCase("d","d"));
    }

    @GetMapping("checkdirectconversation")
    ResponseEntity<?> checkDirectConversation() {
        ConversationFrontendDTO conversationFrontendDTO = conversationService.getConversationBetweenTwoUsers(1L, 2L);
        return ResponseEntity.ok(conversationFrontendDTO);
    }

    @GetMapping("conversationmembers")
    ResponseEntity<?> getConversationMembers() {
        return ResponseEntity.ok(conversationMemberRepository.findConversationMembersByConversationId(1L));
    }

    @GetMapping("getallconversations/{id}")
    ResponseEntity<?> getAllConversations(@PathVariable("id") Long id) {
        return ResponseEntity.ok(conversationService.getAllConversationsOfUser(id));
    }

    @GetMapping("getallusermessages/{id}")
    ResponseEntity<?> getAllUserMessages(@PathVariable("id") Long id) {
        List<ConversationFrontendDTO> conversationFrontendDTO = conversationService.getAllConversationsOfUser(id);

        List<Long> convoIDs = conversationFrontendDTO.stream().map(ConversationFrontendDTO::getConversationId)
                .limit(10)
                .toList();
        System.out.println(convoIDs.toString());
        return ResponseEntity.ok(messageRepository.findLastestInitialMessagesNativeQuery(convoIDs,5));
    }

    @PostMapping("/fetchprevmessages")
    public ResponseEntity<?> fetchprevmessages(@RequestBody Map<String,String> params) {

        Long conversationId = Long.parseLong(params.get("conversationId"));
        Long lastMessageId = params.get("lastMessageId") != null ? Long.parseLong(params.get("lastMessageId")) : null;
        Integer limit = Integer.parseInt(params.get("limit"));

        List<MessageForFrontendDTO> messages = messageService.getPrevMessagesTesting(
                conversationId,
                lastMessageId,
                limit
        );

        boolean is_last_batch = messages.size() < limit;

        Map<String, Object> resp = new HashMap<>();
        resp.put("messages", messages);
        resp.put("is_last_batch", is_last_batch);
        resp.put("last_requested_message_id", lastMessageId);
        resp.put("conversationId", conversationId);

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

    @PostMapping("/insertmessages")
    @Transactional
    public ResponseEntity<?> insertMessages(@RequestBody List<MessageForFrontendDTO> messagesList) {
        List<Message> messages = messagesList.stream().map(m -> {
            User sender = userRepository.findByUserId(m.getSender_user_id())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + m.getSender_user_id()));

            Conversation conversation = conversationRepository.findConversationByConversationId(m.getConversation_id())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + m.getConversation_id()));

            return Message.builder()
                    .id(new MessageId(m.getConversation_id(), m.getMessage_id()))
                    .conversation(conversation)
                    .sender(sender)
                    .content(m.getContent())
                    .isDeleted(m.getIs_deleted())
                    .sentAt(m.getSent_at())
                    .build();
        }).toList();

        messageRepository.saveAll(messages);
        messageRepository.flush(); // ensures @PostPersist triggers now

        return ResponseEntity.ok("Inserted " + messages.size() + " messages successfully");
    }


    @GetMapping("/getconversationmembers/{id}")
    public ResponseEntity<?> findConversationMembersByConversationId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(conversationMemberRepository.findConversationMembersByConversationId(id));
    }

    @GetMapping("/loadinitialdata/{id}")
    public ResponseEntity<Map<String, Object>> getinitialreduxdataBySession(@PathVariable("id") Long userId) {


        Map<String, Object> preloadedstoredata = new HashMap<>();

        List<ConversationFrontendDTO> conversations = conversationService.getAllConversationsOfUser(userId);

        List<Long> convoIDs = conversations.stream().map(ConversationFrontendDTO::getConversationId)
                .limit(10)
                .toList();

        List<MessageForFrontendDTO> messages = messageService.findLastestInitialMessagesNativeQuery(convoIDs,5);

        Map<Long, List<MessageForFrontendDTO>> respmessages = messages.stream().collect(
                Collectors.groupingBy(
                        MessageForFrontendDTO::getConversation_id,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(MessageForFrontendDTO::getMessage_id));
                                    return list;
                                }
                        )
                )
        );

        preloadedstoredata.put("conversations", new HashMap<>(){{
            put("activeconversationobject",null);
            put("conversations", conversations);
        }});
        preloadedstoredata.put("loggeduserdata", userService.findLoggedUserDataByUserId(userId).orElse(null));
        preloadedstoredata.put("conversationmessages",respmessages);

        return ResponseEntity.status(HttpStatus.OK).body(preloadedstoredata);
    }

}
