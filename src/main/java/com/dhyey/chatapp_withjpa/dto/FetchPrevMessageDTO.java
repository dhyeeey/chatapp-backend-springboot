package com.dhyey.chatapp_withjpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchPrevMessageDTO {
    Long conversationId;
    Optional<MessageForFrontendDTO> last_message = Optional.empty();
}
