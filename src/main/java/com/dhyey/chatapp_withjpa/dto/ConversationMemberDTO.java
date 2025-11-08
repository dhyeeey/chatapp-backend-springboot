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
public class ConversationMemberDTO{

    @JsonProperty("user_id")
    Long userId;

    @JsonProperty("avatar")
    String avatar;

    @JsonProperty("profile_name")
    String profileName;

    @JsonProperty("username")
    String username;

    @JsonProperty("user_joined_at")
    Instant joinedAt;

    @JsonProperty("conversation_id")
    Long conversationId;
}

