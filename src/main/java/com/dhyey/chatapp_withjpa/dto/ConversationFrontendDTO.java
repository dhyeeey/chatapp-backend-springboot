package com.dhyey.chatapp_withjpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationFrontendDTO {
    public ConversationFrontendDTO(
            Long conversationId,
            String title,
            Boolean isGroup,
            Instant createdAt
    ) {
        this.conversationId = conversationId;
        this.title = title;
        this.isGroup = isGroup;
        this.createdAt = createdAt;
    }

    @JsonProperty("conversation_id")
    Long conversationId;

    @JsonIgnore
    String title;
    @JsonIgnore
    Boolean isGroup;

    @JsonProperty("last_updated")
    Instant LastUpdated;

    @JsonProperty("conversation_created_at")
    Instant createdAt;

    @JsonProperty("conversation_members")
    List<ConversationMemberDTO> members;

    @JsonProperty("group_info")
    GroupInfoDTO group_info;

    Boolean allMessagesLoaded;
    Boolean isDataFetching;
    Integer dataFetchingCounter;
    Integer websocketOrInputCounter;
}
