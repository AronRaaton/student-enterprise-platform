package cn.com.dhc.controller;

import cn.com.dhc.Entity.ChatMessage;
import cn.com.dhc.mapper.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@org.springframework.context.annotation.Profile("legacy-maintenance")
@RestController
@RequestMapping("/api/chat-test")
public class ChatMessageTestController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/test-save")
    public ResponseEntity<Map<String, Object>> testSaveChatMessage() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Create a test message
            ChatMessage message = new ChatMessage();
            message.setSenderId("1");
            message.setSenderName("Test Sender");
            message.setSenderType("ENTERPRISE");
            message.setReceiverId("2");
            message.setContent("Test message");
            message.setTimestamp(LocalDateTime.now());
            message.setIsRead(false);

            // Save the message
            ChatMessage savedMessage = chatMessageRepository.save(message);

            // Return success response
            response.put("success", true);
            response.put("message", "Test message saved successfully");
            response.put("savedMessage", savedMessage);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response
            response.put("success", false);
            response.put("message", "Error saving test message: " + e.getMessage());
            response.put("error", e.toString());
            response.put("stackTrace", e.getStackTrace());

            return ResponseEntity.status(500).body(response);
        }
    }
}
