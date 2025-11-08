package com.dhyey.chatapp_withjpa.repositories;

import com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO;
import com.dhyey.chatapp_withjpa.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
    SELECT new com.dhyey.chatapp_withjpa.dto.MessageForFrontendDTO(
        m.conversation.conversationId,
        m.id.messageId,
        m.sender.userId,
        m.content,
        m.isDeleted,
        m.sentAt
    )
    FROM Message m
    WHERE m.conversation.conversationId = :conversationId
      AND (:beforeMessageId IS NULL OR m.id.messageId < :beforeMessageId)
    ORDER BY m.id.messageId DESC
    """)
    List<MessageForFrontendDTO> findPreviousMessagesDesc(
            @Param("conversationId") Long conversationId,
            @Param("beforeMessageId") Long beforeMessageId,
            Pageable pageable
    );

    @Query("SELECT COALESCE(MAX(m.id.messageId), 0) + 1 FROM Message m WHERE m.id.conversationId = :conversationId")
    Long findMaxMessageId(@Param("conversationId") Long conversationId);

    @Query(name = "Message.findLatestInitialMessages", nativeQuery = true)
    List<MessageForFrontendDTO> findLastestInitialMessagesNativeQuery(
            @Param("conversationIds") List<Long> conversationIds,
            @Param("lmt") Integer lmt
    );
}
