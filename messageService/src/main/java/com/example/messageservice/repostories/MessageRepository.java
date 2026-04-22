package com.example.messageservice.repostories;

import com.example.messageservice.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE m.conversation.id = :conversationId
        AND m.isDeleted = false
        ORDER BY m.createdAt ASC
    """)
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);

    Optional<Message> findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(Long conversationId);

    @Query("""
        SELECT m FROM Message m
        WHERE m.senderId = :userId
        AND m.isDeleted = false
        ORDER BY m.createdAt DESC
    """)
    List<Message> findBySenderId(@Param("userId") Long userId);
}