package com.dhyey.chatapp_withjpa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageForFrontendDTO {
    @JsonProperty("conversation_id")
    Long conversation_id;

    @JsonProperty("message_id")
    Long message_id;

    @JsonProperty("sender_user_id")
    Long sender_user_id;

    @JsonProperty("content")
    String content;

    @JsonProperty("is_deleted")
    Boolean is_deleted;

    @JsonProperty("sent_at")
    Instant sent_at; // Changed to Instant for TIMESTAMPTZ compatibility
}