package com.dhyey.chatapp_withjpa.entities;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMemberId implements Serializable {

    private Long user;
    private Long conversation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationMemberId)) return false;
        ConversationMemberId that = (ConversationMemberId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(conversation, that.conversation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, conversation);
    }
}
