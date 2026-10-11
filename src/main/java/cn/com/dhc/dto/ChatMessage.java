package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id; // String ID also supports a client-side temporary identifier.
    private String senderId; // 使用String类型，与前端保持一致
    private String senderName;
    private String senderType; // "STUDENT" or "ENTERPRISE"
    private String receiverId; // 使用String类型，与前端保持一致
    private String content;
    private Object timestamp; // Can be LocalDateTime or String
    private boolean read; // Keep this name for compatibility with frontend
}
