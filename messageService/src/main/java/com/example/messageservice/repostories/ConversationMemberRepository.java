package com.example.messageservice.repostories;

import com.example.messageservice.entities.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    @Query("""
        SELECT m FROM ConversationMember m
        WHERE m.conversation.id = :conversationId
        AND m.userId = :userId
    """)
    Optional<ConversationMember> findByConversationAndUserId(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    Optional<ConversationMember> findByConversation_IdAndUsername(Long conversationId, String username);

    Boolean existsByConversation_IdAndUserId(Long conversationId, Long userId);

    List<ConversationMember> findByConversationId(Long conversationId);

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM ConversationMember m
        WHERE m.conversation.id = :conversationId
        AND m.userId = :userId
        AND m.isAdmin = true
    """)
    Boolean isUserAdminOfConversation(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    @Query("""
SELECT cm FROM ConversationMember cm
WHERE cm.conversation.id = :conversationId
""")
    List<ConversationMember> findAllMembers(@Param("conversationId") Long conversationId);
}
