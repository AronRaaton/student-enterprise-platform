package cn.com.dhc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.context.annotation.Profile("legacy-maintenance")
@RestController
@RequestMapping("/api/db-test")
public class DatabaseTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/chat-message-table")
    public ResponseEntity<Map<String, Object>> testChatMessageTable() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get table structure
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM chat_message"
            );
            
            // Get table status
            List<Map<String, Object>> tableStatus = jdbcTemplate.queryForList(
                    "SHOW TABLE STATUS LIKE 'chat_message'"
            );
            
            // Get indexes
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                    "SHOW INDEX FROM chat_message"
            );
            
            // Return all information
            response.put("success", true);
            response.put("columns", columns);
            response.put("tableStatus", tableStatus);
            response.put("indexes", indexes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/test-insert")
    public ResponseEntity<Map<String, Object>> testInsertChatMessage() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Insert a test message directly using SQL
            int result = jdbcTemplate.update(
                    "INSERT INTO chat_message (sender_id, sender_name, sender_type, receiver_id, content, timestamp, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), ?)",
                    "test-sender", "Test Sender", "ENTERPRISE", "test-receiver", "Test message", false
            );
            
            // Get the last inserted ID
            Long lastId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            
            // Return result
            response.put("success", true);
            response.put("rowsAffected", result);
            response.put("lastInsertedId", lastId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(response);
        }
    }
}
