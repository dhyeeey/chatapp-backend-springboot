package com.dhyey.chatapp_withjpa.repositories;

import com.dhyey.chatapp_withjpa.dto.ConversationFrontendDTO;
import com.dhyey.chatapp_withjpa.dto.ConversationMemberDTO;
import com.dhyey.chatapp_withjpa.entities.ConversationMember;
import com.dhyey.chatapp_withjpa.entities.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {

    @Query("""
        SELECT   new com.dhyey.chatapp_withjpa.dto.ConversationMemberDTO(
                         u.userId,
                         u.avatar,
                         u.profileName,
                         u.username,
                         mem.joinedAt,
                         mem.conversation.conversationId
                     )
            FROM ConversationMember mem
            JOIN mem.user u
            WHERE mem.conversation.conversationId = :conversationId
    """)
    List<ConversationMemberDTO> findConversationMembersByConversationId(Long conversationId);
}