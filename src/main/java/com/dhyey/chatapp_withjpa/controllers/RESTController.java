package com.dhyey.chatapp_withjpa.controllers;

import com.dhyey.chatapp_withjpa.dto.ConversationFrontendDTO;
import com.dhyey.chatapp_withjpa.dto.FetchPrevMessageDTO;
import com.dhyey.chatapp_withjpa.dto.GlobalUsersSearch;
import com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO;
import com.dhyey.chatapp_withjpa.services.ConversationService;
import com.dhyey.chatapp_withjpa.services.MessageService;
import com.dhyey.chatapp_withjpa.services.UserService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(
        origins = "http://localhost:3000",
        exposedHeaders = "X-Auth-Token",
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class RESTController {

    private MessageService messageService;
    private ConversationService conversationService;
    private UserService userService;

    @Autowired
    public RESTController(MessageService messageService,
                          ConversationService conversationService,
                          UserService userService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @GetMapping("/loadinitialdata")
    public ResponseEntity<Map<String, Object>> getinitialreduxdataBySession(ServletRequest request){
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpSession session = httpServletRequest.getSession(false);

        Long userId = (Long)session.getAttribute("user_id");

        Map<String, Object> preloadedstoredata = new HashMap<>();

        List<ConversationFrontendDTO> conversations = conversationService.getAllConversationsOfUser(userId);

        List<Long> convoIDs = conversations.stream().map(ConversationFrontendDTO::getConversationId)
                .limit(4)
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

    @PostMapping("/checkconversationexists")
    public ResponseEntity<Map<String, Object>> checkconversationexists(@RequestBody Map<String, Object> body){

        @SuppressWarnings("unchecked")
        Map<String, Object> loggedUserData = (Map<String, Object>) body.get("loggeduserdata");

        @SuppressWarnings("unchecked")
        Map<String, Object> otherUserData = (Map<String, Object>) body.get("otheruserdata");

        Long loggedUserId = loggedUserData.get("user_id") instanceof Number
                ? ((Number) loggedUserData.get("user_id")).longValue()
                : Long.parseLong(loggedUserData.get("user_id").toString());

        Long otherUserId = otherUserData.get("user_id") instanceof Number
                ? ((Number) otherUserData.get("user_id")).longValue()
                : Long.parseLong(otherUserData.get("user_id").toString());

        Optional<Long> conversationId = conversationService.findDirectConversationIdByUserId(otherUserId, loggedUserId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("exists", conversationId.isPresent());
        resp.put("data", conversationId.map(aLong -> Map.of("conversationid", aLong)).orElse(null));

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

    @PostMapping("/users/search")
    public List<GlobalUsersSearch> searchUsers(@RequestBody Map<String, String> body) {
        String searchTerm = body.get("searchTerm");
        return userService.findGlobalUsers(searchTerm, searchTerm);
    }

    @PostMapping("/fetchprevmessages")
    public ResponseEntity<?> fetchprevmessages(
            @RequestBody FetchPrevMessageDTO body
    ){
        Long conversationId = body.getConversationId();
        Optional<MessageForFrontendDTO> optionalMessage = body.getLast_message();

        Long lastMessageId = optionalMessage
                .map(MessageForFrontendDTO::getMessage_id)
                .orElse(null);

        int limit = 20;

        List<MessageForFrontendDTO> messages = messageService.getPrevMessages(conversationId, lastMessageId, limit);

        boolean is_last_batch = messages.size() < limit;

        Map<String, Object> resp = new HashMap<>();
        resp.put("messages", messages);
        resp.put("is_last_batch", is_last_batch);
        resp.put("last_requested_message", optionalMessage.orElse(null));
        resp.put("conversationId", conversationId);

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

}
