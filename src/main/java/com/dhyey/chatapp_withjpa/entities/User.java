package com.dhyey.chatapp_withjpa.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_profilename", columnList = "profile_name")
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @JsonProperty("user_id")
    private Long userId;

    @Column(name="username", unique = true, nullable = false)
    @JsonProperty("username")
    private String username;

    @Column(name = "profile_name", nullable = false)
    @JsonProperty("profile_name")
    private String profileName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP"
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant lastUpdated;

    @ManyToMany
    @JoinTable(
            name = "conversation_members",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "conversation_id")
    )
    @Builder.Default
    @JsonBackReference
    private Set<Conversation> conversations = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    // Helper methods
    public void addConversation(Conversation conversation) {
        this.conversations.add(conversation);
        conversation.getMembers().add(this);
    }

    public void removeConversation(Conversation conversation) {
        this.conversations.remove(conversation);
        conversation.getMembers().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId != null && userId.equals(user.getUserId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
