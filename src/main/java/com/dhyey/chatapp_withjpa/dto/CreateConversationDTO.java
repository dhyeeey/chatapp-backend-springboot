package com.dhyey.chatapp_withjpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateConversationDTO {
    private MessageForFrontendDTO message;
    private LoggedUserData loggeduserdata;
    private GlobalUsersSearch otheruserdata;
    private String token;
    private String type;
}
