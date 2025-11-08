package com.dhyey.chatapp_withjpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "conversation_members")
@IdClass(ConversationMemberId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant joinedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationMember)) return false;
        ConversationMember that = (ConversationMember) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(conversation, that.conversation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, conversation);
    }
}
