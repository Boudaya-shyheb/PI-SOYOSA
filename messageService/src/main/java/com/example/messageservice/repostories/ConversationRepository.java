package com.example.messageservice.repostories;

import com.example.messageservice.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
        SELECT DISTINCT c FROM Conversation c
        INNER JOIN c.members m
        WHERE m.userId = :userId
        ORDER BY c.createdAt DESC
    """)
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT c FROM Conversation c
        INNER JOIN c.members m
        WHERE m.username = :username
        ORDER BY c.createdAt DESC
    """)
    List<Conversation> findAllByUsername(@Param("username") String username);

    @Query("""
        SELECT c FROM Conversation c
        WHERE c.isGroup = false
        AND (SELECT COUNT(m) FROM c.members m) = 2
        AND EXISTS (SELECT 1 FROM c.members m1 WHERE m1.userId = :userId1)
        AND EXISTS (SELECT 1 FROM c.members m2 WHERE m2.userId = :userId2)
    """)
    Optional<Conversation> findPrivateConversationBetween(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    /**
     * Check if user is member of conversation
     */
    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM ConversationMember m
        WHERE m.conversation.id = :conversationId
        AND m.userId = :userId
    """)
    Boolean isUserMemberOfConversation(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );
        @Query("""
        SELECT c
        FROM Conversation c
        JOIN c.members m
        WHERE m.userId = :userId
    """)
        List<Conversation> findByUserId(Long userId);
}