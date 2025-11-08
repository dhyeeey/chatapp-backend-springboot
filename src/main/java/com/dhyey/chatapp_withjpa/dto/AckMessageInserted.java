package com.dhyey.chatapp_withjpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AckMessageInserted {
    String type;
    Long conversationId;
    MessageForFrontendDTO message;
}
