package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Find messages between two users
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findMessagesBetweenUsers(String userId1, String userId2);

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = ?1 AND m.isRead = false")
    int countUnreadMessagesForUser(String userId);

    // Mark messages as read
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiverId = ?1 AND m.senderId = ?2 AND m.isRead = false")
    void markMessagesAsRead(String receiverId, String senderId);
}
