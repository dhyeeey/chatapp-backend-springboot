package com.dhyey.chatapp_withjpa.entities;

import com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO;
import com.dhyey.chatapp_withjpa.exceptions.UnauthorizedMessageException;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@SqlResultSetMapping(
        name = "MessageForFrontendDTOMapping",
        classes = @ConstructorResult(
                targetClass = MessageForFrontendDTO.class,
                columns = {
                        @ColumnResult(name = "conversation_id", type = Long.class),
                        @ColumnResult(name = "message_id", type = Long.class),
                        @ColumnResult(name = "sender_user_id", type = Long.class),
                        @ColumnResult(name = "content", type = String.class),
                        @ColumnResult(name = "is_deleted", type = Boolean.class),
                        @ColumnResult(name = "sent_at", type = Instant.class)
                }
        )
)
@NamedNativeQuery(
        name = "Message.findLatestInitialMessages",
        query = """
            SELECT 
                m.conversation_id,
                m.message_id,
                m.sender_user_id,
                m.content,
                m.is_deleted,
                m.sent_at
            FROM conversations c
            JOIN LATERAL (
                SELECT *
                FROM messages
                WHERE messages.conversation_id = c.conversation_id
                ORDER BY messages.message_id DESC
                LIMIT :lmt
            ) m ON TRUE
            WHERE m.conversation_id IN (:conversationIds)
        """,
        resultSetMapping = "MessageForFrontendDTOMapping"
)
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @EmbeddedId
    private MessageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonBackReference
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    @JsonProperty("is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(
            name = "sent_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP"
    )
    @JsonProperty("sent_at")
    private Instant sentAt;

    public MessageForFrontendDTO getMessageForFrontendDTO() {
        return MessageForFrontendDTO
                .builder()
                .message_id(id.getMessageId())
                .conversation_id(id.getConversationId())
                .sender_user_id(sender.getUserId())
                .content(content)
                .sent_at(sentAt)
                .is_deleted(isDeleted)
                .build();
    }

    @PostPersist
    protected void onPostPersist() {
        if (conversation != null) {
            conversation.updateLastUpdated(this.sentAt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
