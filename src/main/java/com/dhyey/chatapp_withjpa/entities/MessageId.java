package com.dhyey.chatapp_withjpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageId implements Serializable {

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "message_id")
    private Long messageId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageId)) return false;
        MessageId that = (MessageId) o;
        return Objects.equals(conversationId, that.conversationId) &&
                Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, messageId);
    }
}
