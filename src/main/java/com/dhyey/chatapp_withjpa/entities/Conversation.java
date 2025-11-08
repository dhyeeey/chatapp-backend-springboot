package com.dhyey.chatapp_withjpa.entities;

import com.dhyey.chatapp_withjpa.dto.ConversationFrontendDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@NamedNativeQuery(
        name = "ConversationRepository.findDirectConversation",
        query = """
        SELECT c.conversation_id, c.title, c.is_group, c.created_at
        FROM conversations c
        JOIN conversation_members cm ON cm.conversation_id = c.conversation_id
        WHERE c.is_group = false
          AND cm.user_id IN (:user1, :user2)
        GROUP BY c.conversation_id, c.title, c.is_group, c.created_at
        HAVING COUNT(DISTINCT cm.user_id) = 2
    """,
        resultSetMapping = "ConversationDTOMapping"
)
@SqlResultSetMapping(
        name = "ConversationDTOMapping",
        classes = @ConstructorResult(
                targetClass = ConversationFrontendDTO.class,
                columns = {
                        @ColumnResult(name = "conversation_id", type = Long.class),
                        @ColumnResult(name = "title", type = String.class),
                        @ColumnResult(name = "is_group", type = Boolean.class),
                        @ColumnResult(name = "created_at", type = Instant.class)
                }
        )
)
@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    @JsonProperty("conversation_id")
    private Long conversationId;

    @Column(length = 255)
    private String title;

    @Column(name = "is_group", nullable = false)
    @Builder.Default
    @JsonProperty("is_group")
    private Boolean isGroup = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    @JsonProperty("conversation_created_at")
    private Instant createdAt;

    @Column(name = "last_updated", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    @JsonProperty("conversation_last_updated_at")
    private Instant lastUpdated;

    @ManyToMany(mappedBy = "conversations", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private Set<Message> messages = new HashSet<>();

    // Helper methods
    public void addMember(User user) {
        this.members.add(user);
        user.getConversations().add(this);
    }

    public void removeMember(User user) {
        this.members.remove(user);
        user.getConversations().remove(this);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        message.setConversation(this);
    }

    public void removeMessage(Message message) {
        this.messages.remove(message);
        message.setConversation(null);
    }

    public void updateLastUpdated(Instant messageTime) {
        this.lastUpdated = messageTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;
        Conversation that = (Conversation) o;
        return conversationId != null && conversationId.equals(that.getConversationId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
