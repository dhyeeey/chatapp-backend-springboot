package com.dhyey.chatapp_withjpa.repositories;

import com.dhyey.chatapp_withjpa.dto.ConversationFrontendDTO;
import com.dhyey.chatapp_withjpa.entities.Conversation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query(nativeQuery = true, name = "ConversationRepository.findDirectConversation")
    Optional<ConversationFrontendDTO> findDirectConversation(@Param("user1") Long user1, @Param("user2") Long user2);


    @Query("""
        SELECT c.conversationId
        FROM Conversation c
        JOIN c.members m
        WHERE c.isGroup = false
          AND m.userId IN (:user1, :user2)
        GROUP BY c.conversationId
        HAVING COUNT(DISTINCT m.userId) = 2
        """)
    Optional<Long> findDirectConversationIdByUserId(@Param("user1") Long user1, @Param("user2") Long user2);


    Optional<Conversation> findByConversationId(Long conversationId);

    Optional<Conversation> findConversationByConversationId(Long conversationId);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO conversation_members (user_id, conversation_id)
        VALUES (:userId1, :conversationId), (:userId2, :conversationId)
        """, nativeQuery = true)
    void insertUsersInConversation(@Param("conversationId") Long conversationId,
                                   @Param("userId1") Long userId1,
                                   @Param("userId2") Long userId2);


    @Query(value = """
                WITH user_conversations AS (
                                SELECT
                                    user_id,
                                    cnv.conversation_id,
                                    title,
                                    is_group,
                                    last_updated,
                                    cnv_m.joined_at,
                                    cnv.created_at
                                FROM conversation_members cnv_m
                                INNER JOIN conversations cnv ON cnv.conversation_id = cnv_m.conversation_id
                                WHERE user_id = :userId
                                ORDER BY last_updated DESC
                            )
                            SELECT
                                cm.conversation_id,
                                uc.created_at AS conversation_created_at,
                                uc.title,
                                uc.is_group,
                                cm.user_id,
                                u.username,
                                u.profile_name,
                                u.avatar,
                                uc.joined_at AS user_joined_at,
                                uc.last_updated
                            FROM user_conversations uc
                            INNER JOIN conversation_members cm ON cm.conversation_id = uc.conversation_id
                            INNER JOIN users u ON cm.user_id = u.user_id
            """,nativeQuery = true)
    List<Object[]> findConversationsOfUser(@Param("userId") Long userId);
}
